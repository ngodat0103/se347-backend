package com.github.ngodat0103.usersvc.service.kafka;

public interface KafkaService {
  void sendMessage(String topic, Object key, Object message);

  boolean supports(Object key, Object message);
}
