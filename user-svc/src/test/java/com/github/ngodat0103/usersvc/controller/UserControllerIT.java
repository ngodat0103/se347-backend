package com.github.ngodat0103.usersvc.controller;

import com.github.javafaker.Faker;
import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.dto.CredentialDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapperImpl;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
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
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.MapperFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("dev")
class UserControllerIT {
  @Autowired private WebTestClient webTestClient;
  @Autowired private UserRepository userRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String API_PATH = "/api/v1";
  private static final String USER_PATH = API_PATH + "/users";
  private static final String LOGIN_PATH = API_PATH + "/auth/login";
  private static final String DOCKER_IMAGE =
      "mongodb/mongodb-community-server:7.0.6-ubuntu2204-20241117T082517Z";
  private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DOCKER_IMAGE);

  private final UserMapper userMapper = new UserMapperImpl();
  private AccountDto fakeAccountDto;
  private Account fakeAccount;

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
  }

  @Test
  void createAccountWhenNotExists() throws JsonProcessingException {
    webTestClient
        .post()
        .uri(USER_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(objectMapper.writeValueAsString(fakeAccountDto))
        .exchange()
        .expectStatus()
        .isCreated();
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
}
