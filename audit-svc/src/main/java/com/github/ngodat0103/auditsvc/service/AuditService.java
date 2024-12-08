package com.github.ngodat0103.auditsvc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngodat0103.auditsvc.UserElasticSearchRepository;
import com.github.ngodat0103.auditsvc.dto.AccountDto;
import com.github.ngodat0103.auditsvc.dto.Action;
import com.github.ngodat0103.auditsvc.dto.ValueTopicRegisteredUser;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@KafkaListener(topics = "user-cdc", groupId = "audit-svc")
@Slf4j
@AllArgsConstructor
public class AuditService {

  private ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;
  private UserElasticSearchRepository userElasticSearchRepository;
  private ObjectMapper objectMapper;

  @KafkaHandler(isDefault = true)
  public void listen(String message) {
    log.info("Received message: {}", message);
  }

  @KafkaHandler
  public void listen(ValueTopicRegisteredUser value) {
    log.info("Received message from user-cdc topic: {}", value);

    Map<String, Object> additionalProperties = value.getAdditionalProperties();
    Action action = value.getAction();
    AccountDto accountDto =
        objectMapper.convertValue(additionalProperties.get("accountDto"), AccountDto.class);
    sendToElasticsearch(accountDto, action);
  }

  private void sendToElasticsearch(AccountDto accountDto, Action action) {
    if (action.equals(Action.DELETE)) {
      throw new UnsupportedOperationException("Delete operation is not implemented yet");
    }
    if (action.equals(Action.INSERT) || action.equals(Action.UPDATE)) {
      userElasticSearchRepository
          .save(accountDto)
          .doOnSubscribe(s -> log.debug("Saving accountDto to Elasticsearch..."))
          .doOnSuccess(
              s ->
                  log.info(
                      "Successfully send new user {} to Elasticsearch", accountDto.getAccountId()))
          .doOnError(e -> log.error("Error occurred while saving accountDto to Elasticsearch: ", e))
          .subscribe();
    }
  }
}
