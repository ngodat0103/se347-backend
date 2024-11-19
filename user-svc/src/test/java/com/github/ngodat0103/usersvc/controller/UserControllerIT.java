package com.github.ngodat0103.usersvc.controller;

import com.github.javafaker.Faker;
import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.MapperFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIT {
  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private UserRepository userRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String API_PATH = "/api/v1/users";
  private static final String DOCKER_IMAGE =
      "mongodb/mongodb-community-server:7.0.6-ubuntu2204-20241117T082517Z";
  private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DOCKER_IMAGE);

  private final String emailFake = Faker.instance().internet().emailAddress();
  private final String passwordFake = Faker.instance().internet().password();
  private final String nickNameFake = Faker.instance().funnyName().name();

  @BeforeAll
  static void setUpAll() {
    mongoDBContainer.start();
  }

  @AfterAll
  static void tearDownAll() {
    mongoDBContainer.stop();
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.url", mongoDBContainer::getReplicaSetUrl);
  }

  @BeforeEach
  void setUp() {
    objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
  }

  @Test
  @Order(1)
  void createAccountWhenNotExists() throws JsonProcessingException {
    AccountDto accountDto =
        AccountDto.builder().email(emailFake).nickName(nickNameFake).password(passwordFake).build();
    webTestClient
        .post()
        .uri(API_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(objectMapper.writeValueAsString(accountDto))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  @Test
  @Order(2)
  void createAccountWhenAlreadyExists() throws JsonProcessingException {
    AccountDto accountDto =
        AccountDto.builder().email(emailFake).nickName(nickNameFake).password(passwordFake).build();
    Account account =
        Account.builder().email(emailFake).nickName(nickNameFake).password(passwordFake).build();
    userRepository.save(account).block();

    webTestClient
        .post()
        .uri(API_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(objectMapper.writeValueAsString(accountDto))
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
              String expectedDetail = "User with email: " + emailFake + " already exists";
              Assertions.assertEquals(expectedDetail, problemDetail.getDetail());
            });
  }
}
