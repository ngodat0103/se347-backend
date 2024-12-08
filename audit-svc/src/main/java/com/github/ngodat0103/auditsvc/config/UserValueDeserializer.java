package com.github.ngodat0103.auditsvc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngodat0103.auditsvc.dto.ValueTopicRegisteredUser;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;

@AllArgsConstructor
public class UserValueDeserializer implements Deserializer<ValueTopicRegisteredUser> {
  private ObjectMapper objectMapper;

  @Override
  public ValueTopicRegisteredUser deserialize(String topic, byte[] data) {
    try {
      return objectMapper.readValue(data, ValueTopicRegisteredUser.class);
    } catch (Exception e) {
      throw new UnsupportedOperationException("Deserialization error", e);
    }
  }
}
