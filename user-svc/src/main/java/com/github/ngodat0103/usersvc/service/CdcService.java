package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.topic.Action;
import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import com.github.ngodat0103.usersvc.dto.topic.ValueTopicRegisteredUser;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.service.kafka.DefaultKafkaService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
@Configuration
@AllArgsConstructor
public class CdcService implements ApplicationListener<ApplicationReadyEvent> {
  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
  private final UserMapper userMapper;
  private final DefaultKafkaService defaultKafkaService;
  private static final String TOPIC_NAME = "user-cdc";

  public void listenToChanges() {
    reactiveMongoTemplate
        .changeStream(Account.class)
        .watchCollection("accounts")
        .listen()
        .doOnSubscribe(s -> log.info("Subscribed to changes on 'account' collection..."))
        .map(this::mapToAccountAndActionPair)
        .map(this::mapToKafkaMessage)
        .doOnNext(
            pair -> defaultKafkaService.sendMessage(TOPIC_NAME, pair.getLeft(), pair.getRight()))
        .doOnError(throwable -> log.error("Error occurred while listening to changes: ", throwable))
        .doOnTerminate(() -> Thread.currentThread().interrupt())
        .blockLast();
  }

  private Pair<Account, Action> mapToAccountAndActionPair(ChangeStreamEvent<Account> change) {
    log.debug("Change event: {}", change);
    Assert.notNull(change.getOperationType(), "Operation type must not be null");
    Action action = Action.valueOf(change.getOperationType().getValue().toUpperCase());
    if (action.equals(Action.DELETE)) {
      Account account =
          Account.builder()
              .accountId(
                  Objects.requireNonNull(Objects.requireNonNull(change.getRaw()).getDocumentKey())
                      .getString("_id")
                      .getValue())
              .build();
      return Pair.of(account, action);
    } else {
      return Pair.of(change.getBody(), action);
    }
  }

  private Pair<KeyTopic, ValueTopicRegisteredUser> mapToKafkaMessage(Pair<Account, Action> pair) {
    log.debug("Mapping to Kafka message: {}", pair);
    var keyTopic = new KeyTopic("account", pair.getLeft().getAccountId());
    var topicRegisteredUser =
        ValueTopicRegisteredUser.builder()
            .createdDate(LocalDateTime.now().toInstant(ZoneOffset.UTC))
            .action(pair.getRight())
            .additionalProperties(Map.of("accountDto", userMapper.toDto(pair.getLeft())))
            .build();
    return Pair.of(keyTopic, topicRegisteredUser);
  }

  @Override
  public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
    taskExecutor.execute(this::listenToChanges);
  }
}
