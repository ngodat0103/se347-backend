package com.github.ngodat0103.usersvc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngodat0103.usersvc.dto.topic.TopicRegisteredUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    ReactiveRedisTemplate<String, TopicRegisteredUser> reactiveRedisTemplate(ReactiveRedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<TopicRegisteredUser> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, TopicRegisteredUser.class);
        RedisSerializationContext<String, TopicRegisteredUser> serializationContext = RedisSerializationContext
                .<String, TopicRegisteredUser>newSerializationContext()
                .key(stringRedisSerializer)
                .hashKey(stringRedisSerializer)
                .hashValue(jackson2JsonRedisSerializer)
                .value(jackson2JsonRedisSerializer)
                .build();
        return new ReactiveRedisTemplate<>(redisConnectionFactory, serializationContext);
    }

}
