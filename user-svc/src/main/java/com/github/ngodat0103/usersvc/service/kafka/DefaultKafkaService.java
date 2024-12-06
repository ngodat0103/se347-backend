package com.github.ngodat0103.usersvc.service.kafka;

import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import com.github.ngodat0103.usersvc.dto.topic.ValueTopicRegisteredUser;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DefaultKafkaService implements KafkaService {

  private final KafkaTemplate<KeyTopic, ValueTopicRegisteredUser> kafkaTemplate;

  @Override
  public void sendMessage(String topic, Object key, Object message) {
    if (!supports(key, message)) {
      throw new IllegalArgumentException("Key and message not supported");
    }
    kafkaTemplate.send(topic, (KeyTopic) key, (ValueTopicRegisteredUser) message);
  }

  @Override
  public boolean supports(Object key, Object message) {
    return key instanceof KeyTopic && message instanceof ValueTopicRegisteredUser;
  }
}
