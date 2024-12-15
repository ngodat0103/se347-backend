package com.github.ngodat0103.se347_backend.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngodat0103.se347_backend.dto.topic.KeyTopic;
import com.github.ngodat0103.se347_backend.dto.topic.ValueTopicRegisteredUser;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfiguration {
  @Bean
  public KafkaTemplate<KeyTopic, ValueTopicRegisteredUser> kafkaTemplate(
      ObjectMapper objectMapper, KafkaProperties kafkaProperties) {
    Map<String, Object> props =
        Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    Serializer<ValueTopicRegisteredUser> valueSerializer = new JsonSerializer<>(objectMapper);
    Serializer<KeyTopic> keySerializer = new KeyTopicSerializer();
    ProducerFactory<KeyTopic, ValueTopicRegisteredUser> producerFactory =
        new DefaultKafkaProducerFactory<>(props, keySerializer, valueSerializer);
    return new KafkaTemplate<>(producerFactory);
  }
}
