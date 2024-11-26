package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
@Configuration
public class AuditService implements CommandLineRunner {
  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;
//  @Qualifier(SimpleAsyncTaskExecutor.class)
  private final TaskExecutor taskExecutor;
  private final UserMapper userMapper;

  public AuditService(ReactiveMongoTemplate reactiveMongoTemplate,UserMapper userMapper, ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
    this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    this.userMapper = userMapper;
    this.taskExecutor = new SimpleAsyncTaskExecutor();
  }

  public void listenToChanges() {
    reactiveMongoTemplate
        .changeStream(Account.class)
        .watchCollection("account")
        .listen()
            .doOnSubscribe(s -> log.info("Subscribed to changes on 'account' collection..."))
        .map(
            change -> {
                Assert.notNull(change.getBody(), "Change body must not be null");
              Account account = change.getBody();
              account.setPassword(null);
                return userMapper.toDto(account);

            })
            .flatMap(accountDto -> {
                log.info("Push data change to elasticsearch: {}", accountDto);
                return reactiveElasticsearchTemplate.save(accountDto);

            })
            .doOnTerminate(() -> Thread.currentThread().interrupt())
            .blockLast();
  }

    @Override
    public void run(String... args) {
        taskExecutor.execute(this::listenToChanges);
    }
}
