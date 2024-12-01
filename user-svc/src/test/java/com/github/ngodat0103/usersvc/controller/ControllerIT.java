package com.github.ngodat0103.usersvc.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import com.github.ngodat0103.usersvc.dto.account.EmailDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapperImpl;
import com.github.ngodat0103.usersvc.dto.topic.Action;
import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import com.github.ngodat0103.usersvc.dto.topic.TopicRegisteredUser;
import com.github.ngodat0103.usersvc.exception.ConflictException;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.impl.UserServiceImpl;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Assert;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(MyTestConfiguration.class)
@ActiveProfiles("IT")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class ControllerIT {
  @Autowired private WebTestClient webTestClient;
  @Autowired private UserRepository userRepository;
  private final ObjectMapper objectMapper =
      new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
  private static final String API_PATH = "/api/v1";
  private static final String USER_PATH = API_PATH + "/users";
  private static final String LOGIN_PATH = API_PATH + "/auth/login";
  private static final String AUTH_PATH = API_PATH + "/auth";

  private final UserMapper userMapper = new UserMapperImpl();
  private AccountDto fakeAccountDto;
  private Account fakeAccount;
  @Autowired private KafkaConsumer<KeyTopic, TopicRegisteredUser> consumer;
  @Autowired private JwtEncoder jwtEncoder;
  @Autowired private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  private static final List<String> TOPICS = List.of("user-business-logic");

  @BeforeAll
  static void setUpAll() {
    ContainerConfig.startContainers();
  }

  @AfterAll
  static void tearDownAll() {
    ContainerConfig.stopContainers();
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", ContainerConfig::getKafkaBootstrapServers);
    registry.add("spring.data.mongodb.uri", ContainerConfig::getMongoDbUri);
    registry.add("spring.data.redis.port", ContainerConfig::getRedisPort);
  }

  @BeforeEach
  void setUp() {
    this.objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
    Faker faker = new Faker();
    String emailFake = faker.internet().emailAddress();
    String nickNameFake = faker.name().username();
    String passwordFake = faker.internet().password();
    fakeAccountDto = new AccountDto();
    fakeAccountDto.setEmail(emailFake);
    fakeAccountDto.setNickName(nickNameFake);
    fakeAccountDto.setPassword(passwordFake);
    this.fakeAccount = userMapper.toDocument(fakeAccountDto);
    this.fakeAccount.setAccountStatus(Account.AccountStatus.ACTIVE);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll().block();
  }

  @Test
  @Order(1)
  void createAccountWhenNotExists() {
    this.consumer.subscribe(TOPICS);
    webTestClient
        .post()
        .uri(USER_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(fakeAccount)
        .exchange()
        .expectStatus()
        .isCreated();

    var records = consumer.poll(Duration.ofSeconds(3));
    assertEquals(
        1, records.count()); // consume 2 messages from 2 topics: user-business-logic and user-cdc
    var userSvcTopic = records.records(TOPICS.getFirst()).iterator().next();
    var topicRegisteredUser = userSvcTopic.value();
    Assertions.assertNotNull(topicRegisteredUser);
    Assertions.assertNotNull(topicRegisteredUser.getCreatedDate());
    Assertions.assertEquals(Action.INSERT, topicRegisteredUser.getAction());
    Map<String, Object> additionalProperties = topicRegisteredUser.getAdditionalProperties();
    Assertions.assertNotNull(additionalProperties);
    AccountDto accountDto = getAccountDtoFromTopic(topicRegisteredUser);
    Assertions.assertEquals(fakeAccountDto.getEmail(), accountDto.getEmail());
    Assertions.assertEquals(fakeAccountDto.getNickName(), accountDto.getNickName());
    Assertions.assertNull(accountDto.getPassword());
    Assertions.assertNotNull(accountDto.getCreatedDate());
    Assertions.assertNotNull(accountDto.getAccountId());
    Assertions.assertNotNull(accountDto.getLastUpdatedDate());
    EmailDto emailDto = getEmailDtoFromTopic(topicRegisteredUser);
    Assertions.assertEquals(fakeAccountDto.getEmail(), emailDto.getEmail());
    Assertions.assertEquals(accountDto.getAccountId(), emailDto.getAccountId());
    Assertions.assertNotNull(emailDto.getEmailVerificationCode());
    Assertions.assertNotNull(emailDto.getEmailVerificationEndpoint());
  }

  private AccountDto getAccountDtoFromTopic(TopicRegisteredUser topicRegisteredUser) {
    Map<String, Object> additionalProperties = topicRegisteredUser.getAdditionalProperties();
    return objectMapper.convertValue(additionalProperties.get("accountDto"), AccountDto.class);
  }

  private EmailDto getEmailDtoFromTopic(TopicRegisteredUser topicRegisteredUser) {
    Map<String, Object> additionalProperties = topicRegisteredUser.getAdditionalProperties();
    return objectMapper.convertValue(additionalProperties.get("emailDto"), EmailDto.class);
  }

  @Test
  void createAccountWhenAlreadyExists() throws com.fasterxml.jackson.core.JsonProcessingException {
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
              assertEquals(
                  ConflictException.Type.ALREADY_EXISTS.toString(), problemDetail.getTitle());
              assertEquals(
                  "https://problems-registry.smartbear.com/already-exists",
                  problemDetail.getType().toString());
              String expectedDetail =
                  "User with email: " + fakeAccountDto.getEmail() + " already exists";
              assertEquals(expectedDetail, problemDetail.getDetail());
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
              assertEquals("Unauthorized", problemDetail.getTitle());
              assertEquals(
                  "https://problems-registry.smartbear.com/unauthorized",
                  problemDetail.getType().toString());
              String expectedDetail = "Invalid email or password";
              assertEquals(expectedDetail, problemDetail.getDetail());
            });
  }

  @Test
  void givenValidCredential_whenLogin_thenReturnToken()
      throws JsonProcessingException {
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
  void GivenValidToken_whenGetMe_thenReturnAccount() {
    fakeAccount = userRepository.save(fakeAccount).block();
    String accessToken = createTemporaryAccessToken(fakeAccount);
    webTestClient
        .get()
        .uri(USER_PATH + "/me")
        .header("Authorization", "Bearer " + accessToken)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.accountId")
        .isNotEmpty()
        .jsonPath("$.nickName")
        .isEqualTo(fakeAccount.getNickName())
        .jsonPath("$.email")
        .isEqualTo(fakeAccount.getEmail())
        .jsonPath("$.emailVerified")
        .isEqualTo(fakeAccount.isEmailVerified());
  }

  @Test
  void givenAlreadyVerified_whenResendEmail_thenReturn409() {
    fakeAccount.setEmailVerified(true);
    fakeAccount = userRepository.save(fakeAccount).block();
    String accessToken = createTemporaryAccessToken(fakeAccount);
    webTestClient
        .get()
        .uri(AUTH_PATH + "/resend-email")
        .header("Authorization", "Bearer " + accessToken)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT)
        .expectBody(ProblemDetail.class)
        .value(
            problemDetail -> {
              assertEquals(
                  ConflictException.Type.ALREADY_VERIFIED.toString(), problemDetail.getTitle());
              assertEquals(
                  "https://problems-registry.smartbear.com/already-exists",
                  problemDetail.getType().toString());
              String expectedDetail = "Email already verified";
              assertEquals(expectedDetail, problemDetail.getDetail());
            });
  }

  @Test
  @Order(2)
  void givenNotVerified_whenResendEmail_thenReturn202() {
    this.consumer.commitSync();
    fakeAccount.setEmailVerified(false);
    fakeAccount = userRepository.save(fakeAccount).block();
    String accessToken = createTemporaryAccessToken(fakeAccount);
    webTestClient
        .get()
        .uri(AUTH_PATH + "/resend-email")
        .header("Authorization", "Bearer " + accessToken)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.ACCEPTED);
    var records = consumer.poll(Duration.ofSeconds(3));
    assertEquals(1, records.count());
    var recordKafka = records.records(TOPICS.getFirst());
    var topicRegisteredUser = recordKafka.iterator().next().value();
    Assertions.assertNotNull(topicRegisteredUser);
    Assertions.assertEquals(Action.RESEND_EMAIL_VERIFICATION, topicRegisteredUser.getAction());
    EmailDto emailDto = getEmailDtoFromTopic(topicRegisteredUser);
    Assertions.assertNotNull(emailDto);
    Assertions.assertEquals(fakeAccount.getAccountId(), emailDto.getAccountId());
    Assertions.assertEquals(fakeAccount.getEmail(), emailDto.getEmail());
    Assertions.assertNotNull(emailDto.getEmailVerificationCode());
    Assertions.assertNotNull(emailDto.getEmailVerificationEndpoint());
    consumer.commitSync();
  }

  @Test
  void givenValidCode_whenVerifyEmail_thenReturn200() {
    fakeAccount.setEmailVerified(false);
    fakeAccount = userRepository.save(fakeAccount).block();

    String randomCode = UserServiceImpl.generateVerifyEmailCode();
    reactiveRedisTemplate.opsForValue().set(randomCode, fakeAccount.getAccountId()).block();
    String accessToken = createTemporaryAccessToken(fakeAccount);
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(AUTH_PATH + "/verify-email").queryParam("code", randomCode).build())
        .header("Authorization", "Bearer " + accessToken)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(
            message -> {
              assertEquals("Email verified", message);
            });
  }

  private String createTemporaryAccessToken(Account account) {
    Assert.notNull(account, "Account must not be null");
    JwtClaimsSet jwtClaimsSet =
        JwtClaimsSet.builder()
            .subject(account.getAccountId())
            .issuer("user-svc")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(1, ChronoUnit.MINUTES))
            .build();
    JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(jwtClaimsSet);

    return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
  }
}
