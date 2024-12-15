package com.github.ngodat0103.se347_backend.config.kafka;

import com.github.ngodat0103.se347_backend.dto.topic.KeyTopic;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Serializer;

public class KeyTopicSerializer implements Serializer<KeyTopic> {
  @Override
  public byte[] serialize(String s, KeyTopic keyTopic) {
    String key = keyTopic.getDocumentName() + "::" + keyTopic.getAccountId();
    return key.getBytes(StandardCharsets.UTF_8);
  }
}
