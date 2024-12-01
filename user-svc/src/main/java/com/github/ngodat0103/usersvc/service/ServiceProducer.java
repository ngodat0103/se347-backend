package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import com.github.ngodat0103.usersvc.dto.topic.TopicRegisteredUser;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ServiceProducer {
  private KafkaTemplate<KeyTopic, TopicRegisteredUser> producer;
  private static final String TOPIC_BUSINESS_LOGIC = "user-business-logic";
  private static final String TOPIC_CDC = "user-cdc";

  public void sendBusinessLogicTopic(@Valid TopicRegisteredUser topicRegisteredUser) {
    producer.send(TOPIC_BUSINESS_LOGIC, topicRegisteredUser);
  }

  public void sendBusinessLogicTopic(
      KeyTopic keyTopic, @Valid TopicRegisteredUser topicRegisteredUser) {
    log.info("Sending Business Logic message to topic: {}", TOPIC_BUSINESS_LOGIC);
    producer.send(TOPIC_BUSINESS_LOGIC, keyTopic, topicRegisteredUser);
  }

  public void sendCDC(@Valid TopicRegisteredUser topicRegisteredUser) {
    producer.send(TOPIC_CDC, topicRegisteredUser);
  }

  public void sendCDC(KeyTopic key, @Valid TopicRegisteredUser topicRegisteredUser) {
    log.info("Sending CDC message to topic: {}", TOPIC_CDC);
    producer.send(TOPIC_CDC, key, topicRegisteredUser);
  }
}
