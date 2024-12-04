package com.github.ngodat0103.usersvc.service.impl;

import static com.github.ngodat0103.usersvc.exception.Util.*;

import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import com.github.ngodat0103.usersvc.dto.account.EmailDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.topic.Action;
import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import com.github.ngodat0103.usersvc.dto.topic.TopicRegisteredUser;
import com.github.ngodat0103.usersvc.exception.ConflictException;
import com.github.ngodat0103.usersvc.exception.InvalidEmailCodeException;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.ServiceProducer;
import com.github.ngodat0103.usersvc.service.UserService;
import com.nimbusds.jose.util.Base64URL;
import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
  private static final String EMAIL_ALREADY_VERIFIED = "Email already verified";
  private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
  private static final String USER_SVC = "user-svc";
  private static final String EMAIL = "email";
  private static final String USER = "User";
  private static final String IDX_EMAIL = "idx_email";

  private static Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(99);

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final ServiceProducer serviceProducer;
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  private static final URI verifyEmailEndpoint =
      URI.create("http://localhost:5000/api/v1/auth/verify-email");
  private final RedisTemplate<Object, Object> redisTemplate;

  @Override
  public Mono<AccountDto> getMe() {
    return getUserIdFromAuthentication().flatMap(userRepository::findById).map(userMapper::toDto);
  }

  @Override
  public Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto) {
    return userRepository
        .findByEmail(credentialDto.getEmail())
        .filter(
            account -> passwordEncoder.matches(credentialDto.getPassword(), account.getPassword()))
        .switchIfEmpty(Mono.error(new BadCredentialsException(INVALID_EMAIL_OR_PASSWORD)))
        .map(this::createAccessTokenResponse);
  }

  @Override
  public Mono<Void> logout(JwtAuthenticationToken jwtAuthenticationToken) {
    Jwt jwt = jwtAuthenticationToken.getToken();
    String accessToken = jwt.getTokenValue();
    Instant expireAt = jwt.getExpiresAt();
    return reactiveRedisTemplate
        .opsForValue()
        .set(
            "access_token_blacklist:" + accessToken,
            "This user have logout",
            Duration.between(Instant.now(), expireAt))
        .doOnSuccess(aVoid -> log.info("User logged out,blacklist token: {}", accessToken))
        .then();
  }

  private OAuth2AccessTokenResponse createAccessTokenResponse(Account account) {
    Instant now = Instant.now();
    Instant expireAt = now.plusSeconds(ACCESS_TOKEN_DURATION.getSeconds());
    JwtClaimsSet jwtClaimsSet =
        JwtClaimsSet.builder()
            .subject(account.getAccountId())
            .issuedAt(now)
            .expiresAt(expireAt)
            .issuer(USER_SVC)
            .build();
    Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet));
    log.info("User {} logged in", account.getAccountId());
    return OAuth2AccessTokenResponse.withToken(jwt.getTokenValue())
        .tokenType(OAuth2AccessToken.TokenType.BEARER)
        .expiresIn(expireAt.minusSeconds(now.getEpochSecond()).getEpochSecond())
        .build();
  }

  @Override
  public Mono<String> verifyEmail(String code) {
    return reactiveRedisTemplate
        .opsForValue()
        .getAndDelete(code)
        .switchIfEmpty(Mono.defer(() -> Mono.error(InvalidEmailCodeException::new)))
        .flatMap(
            accountId -> {
              log.info("Fetching user with AccountId: {}", accountId);
              return userRepository.findById(accountId);
            })
        .doOnNext(
            account -> {
              log.info("Set email verified for user {}", account.getAccountId());
              account.setEmailVerified(true);
            })
        .flatMap(userRepository::save)
        .map(
            account -> {
              log.info("Email verified for user {}", account.getAccountId());
              return "Email verified";
            });
  }

  @Override
  public Mono<String> resendEmailVerification(ServerHttpRequest request) {
    return getUserIdFromAuthentication()
        .flatMap(userRepository::findById)
        .doOnNext(
            account -> {
              if (account.isEmailVerified()) {
                throw new ConflictException(
                    EMAIL_ALREADY_VERIFIED, ConflictException.Type.ALREADY_VERIFIED);
              }
            })
        .flatMap(
            account -> {
              String verifyEmailCode = generateVerifyEmailCode();
              account.setPassword(null);
              EmailDto emailDto = createEmailDto(account, verifyEmailCode, request.getHeaders());
              Map<String, Object> additionalProperties = Map.of("emailDto", emailDto);
              TopicRegisteredUser topicRegisteredUser =
                  TopicRegisteredUser.builder()
                      .createdDate(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                      .action(Action.RESEND_EMAIL_VERIFICATION)
                      .additionalProperties(additionalProperties)
                      .build();

              log.info("Sending resend email verification to kafka: {}", topicRegisteredUser);
              KeyTopic keyTopic = new KeyTopic("account", account.getAccountId());
              serviceProducer.sendBusinessLogicTopic(keyTopic, topicRegisteredUser);
              return reactiveRedisTemplate
                  .opsForValue()
                  .set(verifyEmailCode, account.getAccountId())
                  .thenReturn(topicRegisteredUser);
            })
        .map(account -> "Email verification sent");
  }

  @Override
  public Mono<AccountDto> create(AccountDto accountDto, ServerHttpRequest request) {

    Account account = userMapper.toDocument(accountDto);
    account.setAccountStatus(Account.AccountStatus.ACTIVE);
    account.setEmailVerified(false);
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    Instant instant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
    account.setCreatedDate(instant);
    account.setLastUpdatedDate(instant);
    account.setWorkspaces(new HashSet<>());
    return userRepository
        .save(account)
            .onErrorMap(DuplicateKeyException.class,e -> createConflictException(log, USER, "email", account.getEmail()))
            .doOnError(ConflictException.class, e ->log.info("Email {} already exists",accountDto.getEmail()))
        .map(a -> Pair.of(account, request.getHeaders()))
        .flatMap(this::handlePostSave);
  }


  private Mono<AccountDto> handlePostSave(Pair<Account, HttpHeaders> pair) {
    Account account = pair.getLeft();
    HttpHeaders headers = pair.getRight();
    String verifyEmailCode = generateVerifyEmailCode();
    AccountDto accountDto = userMapper.toDto(account);
    sendToKafka(account, headers, accountDto, verifyEmailCode)
            .doOnSubscribe(aVoid -> log.debug("Sending email verification to Kafka"))
            .retry(3)
            .doOnSuccess(aVoid -> log.debug("Email verification sent to Kafka"))
            .doOnError(throwable -> log.error("Error sending email verification to Kafka", throwable))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    storeVerificationCodeInRedis(verifyEmailCode, account.getAccountId())
            .doOnSubscribe(aVoid -> log.debug("Storing email verification code in Redis"))
            .retry(3)
            .doOnSuccess(aVoid -> log.debug("Email verification code stored in Redis"))
            .doOnError(throwable -> log.error("Error storing email verification code in Redis", throwable))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    return Mono.just(accountDto);
  }

  private Mono<Void> sendToKafka(Account account, HttpHeaders headers, AccountDto accountDto, String verifyEmailCode) {
    TopicRegisteredUser topicRegisteredUser = createTopicRegisteredUser(account, accountDto, verifyEmailCode,headers);
    KeyTopic keyTopic = new KeyTopic("account", account.getAccountId());
    log.info("Sending new registered user to Kafka: {}", topicRegisteredUser);
    serviceProducer.sendBusinessLogicTopic(keyTopic, topicRegisteredUser);
    return Mono.empty();
  }

  private TopicRegisteredUser createTopicRegisteredUser(Account account, AccountDto accountDto, String verifyEmailCode,HttpHeaders headers) {
    EmailDto emailDto = createEmailDto(account, verifyEmailCode, headers);
    Map<String, Object> additionalProperties = Map.of("accountDto", accountDto, "emailDto", emailDto);
    return TopicRegisteredUser.builder()
        .createdDate(LocalDateTime.now().toInstant(ZoneOffset.UTC))
        .action(Action.INSERT)
        .additionalProperties(additionalProperties)
        .build();
  }

  private Mono<Void> storeVerificationCodeInRedis(String verifyEmailCode, String accountId) {
    log.info("Storing email verification code in Redis: {}", verifyEmailCode);
    return reactiveRedisTemplate.opsForValue().set(verifyEmailCode, accountId).then();
  }




  private EmailDto createEmailDto(
      Account account, String verifyEmailCode, HttpHeaders forwardedHeaders) {

    String emailEndpointUrl =
        ForwardedHeaderUtils.adaptFromForwardedHeaders(verifyEmailEndpoint, forwardedHeaders)
            .query("code=" + verifyEmailCode)
            .build()
            .toUriString();

    EmailDto emailDto = new EmailDto();
    emailDto.setAccountId(account.getAccountId());
    emailDto.setEmailVerificationCode(verifyEmailCode);
    emailDto.setEmailVerificationEndpoint(emailEndpointUrl);
    emailDto.setEmail(account.getEmail());
    return emailDto;
  }

  public static String generateVerifyEmailCode() {
    byte[] randomBytes = new byte[16];
    SecureRandom random = new SecureRandom();
    random.nextBytes(randomBytes);
    return Base64URL.encode(randomBytes).toString();
  }

  @Override
  public Mono<AccountDto> update(AccountDto accountDto) {
    return Mono.empty();
  }

  @Override
  public Mono<Void> delete(String id) {
    return Mono.empty();
  }

  @Override
  public Mono<AccountDto> get(String id) {
    return Mono.empty();
  }
}
