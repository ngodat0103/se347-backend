package com.github.ngodat0103.usersvc.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.Faker;
import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapperImpl;
import com.github.ngodat0103.usersvc.dto.topic.Action;
import com.github.ngodat0103.usersvc.exception.ConflictException;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.document.workspace.Workspace;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.usersvc.service.email.UserEmailService;
import com.jayway.jsonpath.JsonPath;
import com.redis.testcontainers.RedisContainer;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.MapperFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("IT")
@Slf4j
class ControllerIT {
  @Autowired private WebTestClient webTestClient;
  @Autowired private UserRepository userRepository;
  @Autowired private WorkspaceRepository workspaceRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String API_PATH = "/api/v1";
  private static final String USER_PATH = API_PATH + "/users";
  private static final String LOGIN_PATH = API_PATH + "/auth/login";
  private static final String AUTH_PATH = API_PATH + "/auth";
  private static final String MONGODB_DOCKER_IMAGE =
      "mongodb/mongodb-community-server:7.0.6-ubuntu2204-20241117T082517Z";
  private static final MongoDBContainer mongoDBContainer =
      new MongoDBContainer(MONGODB_DOCKER_IMAGE);

  private static final RedisContainer redisContainer =
      new RedisContainer(DockerImageName.parse("redis:alpine3.20"));

  private static final String KAFKA_DOCKER_IMAGE = "confluentinc/cp-kafka:7.4.6";

  private static final ConfluentKafkaContainer kafkaContainer =
      new ConfluentKafkaContainer(KAFKA_DOCKER_IMAGE);

  private static final MinIOContainer minioContainer =
      new MinIOContainer(DockerImageName.parse("minio/minio:RELEASE.2023-09-04T19-57-37Z"));
  private final UserMapper userMapper = new UserMapperImpl();
  private AccountDto fakeAccountDto;
  private Account fakeAccount;
  private KafkaConsumer<String, String> consumer;
  @Autowired private JwtEncoder jwtEncoder;
  @Autowired private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  private static final List<String> TOPICS = List.of("user-email");
  private static final Faker faker = new Faker();

  @BeforeAll
  static void setUpAll() {
    mongoDBContainer.start();
    kafkaContainer.start();
    redisContainer.start();
    minioContainer.start();
  }

  @AfterAll
  static void tearDownAll() {
    mongoDBContainer.stop();
    kafkaContainer.stop();
    redisContainer.stop();
    minioContainer.stop();
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    registry.add("spring.data.redis.port", redisContainer::getRedisPort);
    registry.add("minio.url", minioContainer::getS3URL);
    registry.add("minio.access-key", minioContainer::getUserName);
    registry.add("minio.secret-key", minioContainer::getPassword);
  }

  @BeforeEach
  void setUp() {
    this.objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
    String emailFake = faker.internet().emailAddress();
    String nickNameFake = faker.name().username();
    String passwordFake = faker.internet().password();
    this.fakeAccountDto =
        AccountDto.builder().email(emailFake).nickName(nickNameFake).password(passwordFake).build();
    this.fakeAccount = userMapper.toDocument(fakeAccountDto);
    this.fakeAccount.setAccountStatus(Account.AccountStatus.ACTIVE);
    this.consumer = new KafkaConsumer<>(getConsumerProps());
    this.consumer.subscribe(TOPICS);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll().block();
    consumer.close();
  }

  @Test
  void createAccountWhenNotExists() {
    webTestClient
        .post()
        .uri(USER_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(fakeAccount)
        .exchange()
        .expectStatus()
        .isCreated();

    var records = consumer.poll(Duration.ofSeconds(3));
    assertEquals(1, records.count());
    var recordKafka = records.iterator().next();
    var value = recordKafka.value();
    assertNotNull(JsonPath.read(value, "$.createdDate"));
    assertEquals(Action.INSERT.toString(), JsonPath.read(value, "$.action"));
    assertNotNull(JsonPath.read(value, "$.additionalProperties.emailDto.accountId"));
    assertNotNull(JsonPath.read(value, "$.additionalProperties.emailDto.emailVerificationCode"));
    assertNotNull(
        JsonPath.read(value, "$.additionalProperties.emailDto.emailVerificationEndpoint"));
    assertEquals(
        fakeAccount.getEmail(), JsonPath.read(value, "$.additionalProperties.emailDto.email"));
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
  void givenNotVerified_whenResendEmail_thenReturn202() {
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
    var records = consumer.poll(Duration.ofSeconds(5));
    assertEquals(1, records.count());
    var recordKafka = records.iterator().next();
    var value = recordKafka.value();
    log.debug(value);
    String accountId = JsonPath.read(value, "$.additionalProperties.emailDto.accountId");
    String email = JsonPath.read(value, "$.additionalProperties.emailDto.email");
    assertEquals(fakeAccountDto.getEmail(), email);
    Assertions.assertNotNull(accountId);
    assertEquals(Action.RESEND_EMAIL_VERIFICATION.toString(), JsonPath.read(value, "$.action"));
  }

  @Test
  void givenValidCode_whenVerifyEmail_thenReturn200() {
    fakeAccount.setEmailVerified(false);
    fakeAccount = userRepository.save(fakeAccount).block();

    String randomCode = UserEmailService.generateVerifyEmailCode();
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

  @Test
  void createWorkspace_thenReturnSuccessful() {
    // Given
    Account account = userRepository.save(fakeAccount).block();
    String accessToken = createTemporaryAccessToken(account);
    WorkspaceDto workspaceDto = WorkspaceDto.builder().name(faker.funnyName().name()).build();
    // When
    webTestClient
        .post()
        .uri(API_PATH + "/workspaces")
        .header("Authorization", "Bearer " + accessToken)
        .bodyValue(workspaceDto)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.id")
        .isNotEmpty()
        .jsonPath("$.name")
        .isEqualTo(workspaceDto.getName())
        .jsonPath("$.projects")
        .isEmpty()
        .jsonPath("$.imageUrl")
        .isEmpty()
        .jsonPath("$.createdDate")
        .isNotEmpty()
        .jsonPath("$.lastUpdatedDate")
        .isNotEmpty();
  }

  @Test
  void givenAlreadyExistsWorkspace_whenCreateWorkspace_thenReturn409() {
    // Given
    Account account = userRepository.save(fakeAccount).block();
    String accessToken = createTemporaryAccessToken(account);
    WorkspaceDto workspaceDto = WorkspaceDto.builder().name(faker.funnyName().name()).build();
    webTestClient
        .post()
        .uri(API_PATH + "/workspaces")
        .header("Authorization", "Bearer " + accessToken)
        .bodyValue(workspaceDto)
        .exchange()
        .expectStatus()
        .isCreated();
    // When
    webTestClient
        .post()
        .uri(API_PATH + "/workspaces")
        .header("Authorization", "Bearer " + accessToken)
        .bodyValue(workspaceDto)
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
                  "workspace with name: " + workspaceDto.getName() + " already exists";
              assertEquals(expectedDetail, problemDetail.getDetail());
            });
  }

  @Test
  void givenHaveWorkspace_whenGetWorkspaces_thenReturnWorkspaces() {
    // Given
    Account account = userRepository.save(fakeAccount).block();
    String accessToken = createTemporaryAccessToken(account);
    Workspace workspace = new Workspace();
    workspace.setName(faker.funnyName().name());
    workspace.setLastUpdatedDate(Instant.now());
    workspace.setCreatedDate(Instant.now());
    assert account != null;
    workspace.setOwner(account.getAccountId());
    var workspaceResponse = workspaceRepository.save(workspace).block();
    assert workspaceResponse != null;
    // When
    webTestClient
        .get()
        .uri(API_PATH + "/workspaces/me")
        .header("Authorization", "Bearer " + accessToken)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].id")
        .isEqualTo(workspaceResponse.getId())
        .jsonPath("$[0].name")
        .isEqualTo(workspaceResponse.getName())
        .jsonPath("$[0].projects")
        .isEmpty()
        .jsonPath("$[0].imageUrl")
        .isEmpty()
        .jsonPath("$[0].createdDate")
        .isNotEmpty()
        .jsonPath("$[0].lastUpdatedDate")
        .isNotEmpty();
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
