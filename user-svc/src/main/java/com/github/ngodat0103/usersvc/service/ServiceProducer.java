package com.github.ngodat0103.usersvc.service;

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
  private KafkaTemplate<String, TopicRegisteredUser> producer;

  public void sendRegisteredUser(@Valid TopicRegisteredUser topicRegisteredUser) {
    log.debug("Sending new registered user: {}", topicRegisteredUser);
    producer.send("new-registered-user", topicRegisteredUser);
  }
}
