package com.github.ngodat0103.usersvc.config.kafka;

import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Serializer;

public class KeyTopicSerializer implements Serializer<KeyTopic> {
  @Override
  public byte[] serialize(String s, KeyTopic keyTopic) {
    String key = keyTopic.getDocumentName() + "::" + keyTopic.getAccountId();
    return key.getBytes(StandardCharsets.UTF_8);
  }
}
