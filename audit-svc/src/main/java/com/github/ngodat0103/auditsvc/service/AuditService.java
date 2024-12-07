package com.github.ngodat0103.auditsvc.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngodat0103.auditsvc.dto.AccountDto;
import com.github.ngodat0103.auditsvc.dto.TopicRegisteredUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@KafkaListener(topics = "registered-user", groupId = "audit-svc")
@Slf4j
@AllArgsConstructor
public class AuditService {

    private ObjectMapper objectMapper ;
    private ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    @KafkaHandler(isDefault = true)
    public void listen(String message) throws JsonProcessingException {
        System.out.println("Received Messasge in group audit-svc: " + message);
        TopicRegisteredUser topicRegisteredUser = objectMapper.readValue(message, TopicRegisteredUser.class);
        TopicRegisteredUser.Action action = topicRegisteredUser.getAction();
        switch (action) {
            case NEW_USER:
                AccountDto accountDto = objectMapper.convertValue(topicRegisteredUser.getAdditionalProperties().get("accountDto"), AccountDto.class);
                reactiveElasticsearchTemplate.save(accountDto)
                        .doOnError(throwable -> log.error("Error saving accountDto: {}", accountDto))
                        .doOnSuccess(accountDto1 -> log.info("Saved accountDto: {}", accountDto1))
                        .subscribe();
                break;
            case RESET_PASSWORD:
                break;
            case RESEND_EMAIL_VERIFICATION:
                break;
            case DATA_UPDATE:
                break;
        }


    }
}
