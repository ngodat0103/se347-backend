package com.github.ngodat0103.usersvc.controller;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class ContainerConfig {
  private static final String MONGODB_DOCKER_IMAGE =
      "mongodb/mongodb-community-server:7.0.6-ubuntu2204-20241117T082517Z";
  private static final MongoDBContainer mongoDBContainer =
      new MongoDBContainer(MONGODB_DOCKER_IMAGE);

  private static final RedisContainer redisContainer =
      new RedisContainer(DockerImageName.parse("redis:alpine3.20"));

  private static final String KAFKA_DOCKER_IMAGE = "confluentinc/cp-kafka:7.4.6";

  private static final ConfluentKafkaContainer kafkaContainer =
      new ConfluentKafkaContainer(KAFKA_DOCKER_IMAGE);

  public static void startContainers() {
    mongoDBContainer.start();
    redisContainer.start();
    kafkaContainer.start();
  }

  public static void stopContainers() {
    mongoDBContainer.stop();
    redisContainer.stop();
    kafkaContainer.stop();
  }

  public static String getMongoDbUri() {
    return mongoDBContainer.getReplicaSetUrl();
  }

  public static int getRedisPort() {
    return redisContainer.getRedisPort();
  }

  public static String getKafkaBootstrapServers() {
    return kafkaContainer.getBootstrapServers();
  }
}
