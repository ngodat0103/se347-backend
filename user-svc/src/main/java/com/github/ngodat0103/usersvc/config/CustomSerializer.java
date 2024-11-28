package com.github.ngodat0103.usersvc.config;

import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Serializer;

public class CustomSerializer implements Serializer<KeyTopic> {
  @Override
  public byte[] serialize(String s, KeyTopic keyTopic) {
    String key = keyTopic.getDocumentName() + "::" + keyTopic.getUserId();
    return key.getBytes(StandardCharsets.UTF_8);
  }
}
