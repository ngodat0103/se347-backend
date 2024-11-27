package com.github.ngodat0103.usersvc.service.impl;

import static com.github.ngodat0103.usersvc.exception.Util.*;

import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.dto.CredentialDto;
import com.github.ngodat0103.usersvc.dto.EmailDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.topic.TopicRegisteredUser;
import com.github.ngodat0103.usersvc.exception.ConflictException;
import com.github.ngodat0103.usersvc.exception.InvalidEmailCodeException;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.ServiceProducer;
import com.github.ngodat0103.usersvc.service.UserService;
import com.nimbusds.jose.util.Base64URL;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

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

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final ServiceProducer serviceProducer;
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

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

  private OAuth2AccessTokenResponse createAccessTokenResponse(Account account) {
    Instant now = Instant.now();
    Instant expireAt = now.plusSeconds(Duration.ofMinutes(5).getSeconds());
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
  public Mono<String> resendEmailVerification() {
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
              EmailDto emailDto = createEmailDto(account, verifyEmailCode);
              Map<String, Object> additionalProperties = Map.of("emailDto",emailDto);
              TopicRegisteredUser topicRegisteredUser =
                  userMapper.toTopicRegisteredUser(
                      account,
                      TopicRegisteredUser.Action.RESEND_EMAIL_VERIFICATION,
                      additionalProperties);
              log.info("Sending resend email verification to kafka: {}", topicRegisteredUser);
              return reactiveRedisTemplate
                  .opsForValue()
                  .set(verifyEmailCode, account.getAccountId())
                  .thenReturn(topicRegisteredUser);
            })
        .doOnNext(serviceProducer::sendRegisteredUser)
        .map(account -> "Email verification sent");
  }

  @Override
  @Transactional
  public Mono<AccountDto> create(AccountDto accountDto) {
    Account account = userMapper.toDocument(accountDto);
    account.setAccountStatus(Account.AccountStatus.ACTIVE);
    account.setEmailVerified(false);
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    account.setCreatedDate(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    account.setLastUpdatedDate(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    return userRepository
        .save(account)
        .doOnError(e -> handleSaveError(e, account))
        .flatMap(this::handlePostSave);
  }

  private void handleSaveError(Throwable e, Account account) {
    if (e.getMessage().contains(IDX_EMAIL)) {
      log.info("User with email {} already exists", account.getEmail());
      throw createConflictException(log, USER, EMAIL, account.getEmail());
    }
    log.error("Unexpected exception", e);
  }

  private Mono<AccountDto> handlePostSave(Account account) {
    String verifyEmailCode = generateVerifyEmailCode();
    EmailDto emailDto = createEmailDto(account, verifyEmailCode);
    AccountDto accountDto = userMapper.toDto(account);
    Map<String, Object> additionalProperties =
        Map.of("accountDto", accountDto, "emailDto", emailDto);
    TopicRegisteredUser topicRegisteredUser =
        userMapper.toTopicRegisteredUser(
            account, TopicRegisteredUser.Action.NEW_USER, additionalProperties);
    log.info("Sending new registered user to kafka: {}", topicRegisteredUser);
    serviceProducer.sendRegisteredUser(topicRegisteredUser);
    log.info("Store Email verification code in redis: {}", verifyEmailCode);
    return reactiveRedisTemplate
        .opsForValue()
        .set(verifyEmailCode, account.getAccountId())
        .thenReturn(account)
        .map(userMapper::toDto);
  }

  private EmailDto createEmailDto(Account account, String verifyEmailCode) {
    String emailEndpoint =
        UriComponentsBuilder.fromUriString("http://localhost:5000/api/v1/users/verify-email")
            .queryParam("code", verifyEmailCode)
            .build()
            .toUriString();
    return EmailDto.builder()
        .accountId(account.getAccountId())
        .emailVerificationCode(verifyEmailCode)
        .emailVerificationEndpoint(emailEndpoint)
        .email(account.getEmail())
        .build();
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
  public Mono<AccountDto> delete(AccountDto accountDto) {
    return Mono.empty();
  }

  @Override
  public Mono<AccountDto> get(AccountDto accountDto) {
    return Mono.empty();
  }
}
