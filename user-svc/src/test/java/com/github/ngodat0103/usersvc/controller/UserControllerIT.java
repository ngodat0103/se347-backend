package com.github.ngodat0103.usersvc.controller;

import com.github.javafaker.Faker;
import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.dto.CredentialDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapperImpl;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.MapperFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("dev")
@Slf4j
class UserControllerIT {
  @Autowired private WebTestClient webTestClient;
  @Autowired private UserRepository userRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String API_PATH = "/api/v1";
  private static final String USER_PATH = API_PATH + "/users";
  private static final String LOGIN_PATH = API_PATH + "/auth/login";
  private static final String MONGODB_DOCKER_IMAGE =
      "mongodb/mongodb-community-server:7.0.6-ubuntu2204-20241117T082517Z";
  private static final MongoDBContainer mongoDBContainer =
      new MongoDBContainer(MONGODB_DOCKER_IMAGE);

  private static final String KAFKA_DOCKER_IMAGE = "confluentinc/cp-kafka:7.4.6";

  private static final ConfluentKafkaContainer kafkaContainer =
      new ConfluentKafkaContainer(KAFKA_DOCKER_IMAGE);
  private final UserMapper userMapper = new UserMapperImpl();
  private AccountDto fakeAccountDto;
  private Account fakeAccount;
  private KafkaConsumer<String, String> consumer;

  @BeforeAll
  static void setUpAll() {
    mongoDBContainer.start();
    kafkaContainer.start();
  }

  @AfterAll
  static void tearDownAll() {
    mongoDBContainer.stop();
    kafkaContainer.stop();
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @BeforeEach
  void setUp() {
    Faker faker = new Faker();
    String emailFake = faker.internet().emailAddress();
    String nickNameFake = faker.name().username();
    String passwordFake = faker.internet().password();
    objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
    this.fakeAccountDto =
        AccountDto.builder().email(emailFake).nickName(nickNameFake).password(passwordFake).build();
    this.fakeAccount = userMapper.toDocument(fakeAccountDto);
    this.consumer = new KafkaConsumer<>(getConsumerProps());
    this.consumer.subscribe(List.of("new-registered-user"));
  }

  @Test
  void createAccountWhenNotExists() throws IOException {

    webTestClient
        .post()
        .uri(USER_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(objectMapper.writeValueAsString(fakeAccountDto))
        .exchange()
        .expectStatus()
        .isCreated();

    var records = consumer.poll(Duration.ofSeconds(5));
    Assertions.assertEquals(1, records.count());
    var record = records.iterator().next();
    var value = record.value();
    log.debug(value);
    String accountId = JsonPath.read(value, "$.accountId");
    String email = JsonPath.read(value, "$.email");
    Assertions.assertEquals(fakeAccountDto.getEmail(), email);
    Assertions.assertNotNull(accountId);
  }

  @Test
  void createAccountWhenAlreadyExists() throws JsonProcessingException {
    userRepository.save(fakeAccount).block();

    webTestClient
        .post()
        .uri(USER_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(objectMapper.writeValueAsString(fakeAccountDto))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT)
        .expectBody(ProblemDetail.class)
        .value(
            problemDetail -> {
              Assertions.assertEquals("Already exists", problemDetail.getTitle());
              Assertions.assertEquals(
                  "https://problems-registry.smartbear.com/already-exists",
                  problemDetail.getType().toString());
              String expectedDetail =
                  "User with email: " + fakeAccountDto.getEmail() + " already exists";
              Assertions.assertEquals(expectedDetail, problemDetail.getDetail());
            });
  }

  @Test
  void givenBadCredential_whenLogin_thenReturn401() throws JsonProcessingException {
    CredentialDto credentialDto =
        CredentialDto.builder()
            .email(fakeAccountDto.getEmail())
            .password(fakeAccountDto.getPassword())
            .build();
    webTestClient
        .post()
        .uri(LOGIN_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(objectMapper.writeValueAsString(credentialDto))
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody(ProblemDetail.class)
        .value(
            problemDetail -> {
              Assertions.assertEquals("Unauthorized", problemDetail.getTitle());
              Assertions.assertEquals(
                  "https://problems-registry.smartbear.com/unauthorized",
                  problemDetail.getType().toString());
              String expectedDetail = "Invalid email or password";
              Assertions.assertEquals(expectedDetail, problemDetail.getDetail());
            });
  }

  @Test
  void givenValidCredential_whenLogin_thenReturnToken() throws JsonProcessingException {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    fakeAccount.setPassword(passwordEncoder.encode(fakeAccountDto.getPassword()));
    userRepository.save(fakeAccount).block();
    CredentialDto credentialDto =
        CredentialDto.builder()
            .email(fakeAccountDto.getEmail())
            .password(fakeAccountDto.getPassword())
            .build();
    webTestClient
        .post()
        .uri(LOGIN_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(objectMapper.writeValueAsString(credentialDto))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.accessToken.tokenValue")
        .isNotEmpty()
        .jsonPath("$.accessToken.tokenType.value")
        .isEqualTo("Bearer")
        .jsonPath("$.accessToken.expiresAt")
        .isNotEmpty()
        .jsonPath("$.accessToken.issuedAt")
        .isNotEmpty();
  }

  @Test
  @Disabled(value = "Not implemented yet")
  void GivenValidToken_whenGetMe_thenReturnAccount() {
    throw new UnsupportedOperationException("Not implemented yet");
    // todo: implement this test
  }

  private Map<String, Object> getConsumerProps() {
    return Map.of(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        "earliest",
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        kafkaContainer.getBootstrapServers(),
        ConsumerConfig.GROUP_ID_CONFIG,
        "test-group",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class);
  }
}
