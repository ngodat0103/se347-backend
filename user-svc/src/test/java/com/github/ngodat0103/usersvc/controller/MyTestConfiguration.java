package com.github.ngodat0103.usersvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import com.github.ngodat0103.usersvc.dto.topic.TopicRegisteredUser;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@TestConfiguration
public class MyTestConfiguration {

  @Bean
  public KafkaConsumer<KeyTopic, TopicRegisteredUser> kafkaConsumer() {
    Deserializer<KeyTopic> keyDeserializer = new KeyTopicDeserializer();
    ObjectMapper objectMapper =
        new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    Deserializer<TopicRegisteredUser> valueDeserializer =
        new JsonDeserializer<>(TopicRegisteredUser.class, objectMapper);

    return new KafkaConsumer<>(consumerProps(), keyDeserializer, valueDeserializer);
  }

  private Map<String, Object> consumerProps() {
    return Map.of(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ContainerConfig.getKafkaBootstrapServers(),
        ConsumerConfig.GROUP_ID_CONFIG, "test-group",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
  }

  private class KeyTopicDeserializer implements Deserializer<KeyTopic> {
    @Override
    public KeyTopic deserialize(String topic, byte[] data) {
      String key = new String(data);
      String documentName = key.split("::")[0];
      String accountId = key.split("::")[1];
      return new KeyTopic(documentName, accountId);
    }
  }
}
