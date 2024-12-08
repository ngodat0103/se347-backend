package com.github.ngodat0103.auditsvc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngodat0103.auditsvc.dto.UserKeyTopic;
import com.github.ngodat0103.auditsvc.dto.ValueTopicRegisteredUser;
import java.util.Map;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConfiguration {

  @Bean
  ConsumerFactory<UserKeyTopic, ValueTopicRegisteredUser> kafkaConsumer(
      ObjectMapper objectMapper, KafkaProperties kafkaProperties) {
    var keyDeserializer = new UserKeyDeserializer();
    var valueDeserializer = new UserValueDeserializer(objectMapper);
    KafkaProperties.Consumer consumer = kafkaProperties.getConsumer();
    Map<String, Object> props =
        Map.of(
            "bootstrap.servers", consumer.getBootstrapServers(),
            "group.id", consumer.getGroupId(),
            "enable.auto.commit", consumer.getEnableAutoCommit(),
            "auto.offset.reset", consumer.getAutoOffsetReset());
    return new DefaultKafkaConsumerFactory<>(props, keyDeserializer, valueDeserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<UserKeyTopic, ValueTopicRegisteredUser>
      kafkaListenerContainerFactory(
          ConsumerFactory<UserKeyTopic, ValueTopicRegisteredUser> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<UserKeyTopic, ValueTopicRegisteredUser> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }
}
