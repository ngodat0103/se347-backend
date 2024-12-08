package com.github.ngodat0103.auditsvc.config;

import com.github.ngodat0103.auditsvc.dto.UserKeyTopic;
import org.apache.kafka.common.serialization.Deserializer;

public class UserKeyDeserializer implements Deserializer<UserKeyTopic> {
  @Override
  public UserKeyTopic deserialize(String topic, byte[] data) {
    String keyString = new String(data);
    String documentName = keyString.split("::")[0];
    String accountId = keyString.split("::")[1];
    return new UserKeyTopic(documentName, accountId);
  }
}
