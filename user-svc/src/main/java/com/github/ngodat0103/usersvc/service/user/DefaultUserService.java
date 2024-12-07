package com.github.ngodat0103.usersvc.service.user;

import static com.github.ngodat0103.usersvc.exception.Util.*;

import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapperImpl;
import com.github.ngodat0103.usersvc.exception.ConflictException;
import com.github.ngodat0103.usersvc.exception.InvalidEmailCodeException;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.auth.AuthService;
import com.github.ngodat0103.usersvc.service.auth.JwtAuthService;
import com.github.ngodat0103.usersvc.service.email.EmailService;
import com.github.ngodat0103.usersvc.service.email.UserEmailService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultUserService implements UserService {
  private static final String EMAIL_ALREADY_VERIFIED = "Email already verified";
    private static final String EMAIL = "email";
  private static final String USER = "User";
  private final UserMapper userMapper = new UserMapperImpl();
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;

  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
  private final EmailService emailService;
  private final AuthService authService;

  @Override
  public Mono<AccountDto> getMe() {
    return getUserIdFromAuthentication().flatMap(userRepository::findById).map(userMapper::toDto);
  }

  @Override
  public Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto) {
    return authService.login(credentialDto);

  }

  @Override
  public Mono<Void> logout(Authentication authentication) {
    return authService.logout(authentication);
  }

  @Override
  public Mono<String> verifyEmail(String code) {
    return reactiveRedisTemplate
        .opsForValue()
        .getAndDelete(code)
        .switchIfEmpty(Mono.defer(() -> Mono.error(InvalidEmailCodeException::new)))
        .flatMap(userRepository::findById)
        .flatMap(this::updatedEmailVerified)
        .doOnSuccess(account -> log.info("Email verified for user {}", account.getAccountId()))
        .doOnError(
            e -> !(e instanceof InvalidEmailCodeException), e -> log.error("Not expected error", e))
        .thenReturn("Email verified");
  }

  private Mono<Account> updatedEmailVerified(Account account) {
    log.debug("Set email verified for user {}", account.getAccountId());
    account.setEmailVerified(true);
    return userRepository.save(account);
  }

  @Override
  public Mono<String> resendEmailVerification(ServerHttpRequest request) {
    return getUserIdFromAuthentication()
        .flatMap(userRepository::findById)
        .flatMap(this::validateEmailVerificationStatus)
        .map(userMapper::toDto)
        .flatMap(account -> emailService.resendEmailVerification(account, request.getHeaders()))
        .thenReturn("Email verification code resent");
  }

  private Mono<Account> validateEmailVerificationStatus(Account account) {
    if (account.isEmailVerified()) {
      return Mono.error(
          new ConflictException(EMAIL_ALREADY_VERIFIED, ConflictException.Type.ALREADY_VERIFIED));
    }
    return Mono.just(account);
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
        .onErrorMap(
            DuplicateKeyException.class,
            e -> createConflictException(log, USER, EMAIL, account.getEmail()))
        .doOnError(
            ConflictException.class,
            e -> log.info("Email {} already exists", accountDto.getEmail()))
        .map(userMapper::toDto)
        .map(a -> Pair.of(a, request.getHeaders()))
        .flatMap(this::handlePostSave)
        .doOnSuccess(a -> log.info("Create new user with email {} successfully", a.getEmail()));
  }

  private Mono<AccountDto> handlePostSave(Pair<AccountDto, HttpHeaders> pair) {
    AccountDto accountDto = pair.getLeft();
    HttpHeaders forwardedHeaders = pair.getRight();
    triggerEmailServiceAsync(accountDto, forwardedHeaders);
    return Mono.just(accountDto);
  }

  private void triggerEmailServiceAsync(AccountDto accountDto, HttpHeaders forwardedHeaders) {


    emailService
        .emailNewUser(accountDto, forwardedHeaders)
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSubscribe(s -> log.debug("Starting email task for user: {}", accountDto.getEmail()))
        .doOnSuccess(
            unused -> log.info("Email successfully sent for user: {}", accountDto.getEmail()))
        .doOnError(
            error -> log.error("Failed to send email for user: {}", accountDto.getEmail(), error))
        .subscribe();
  }

  @Override
  public Mono<AccountDto> get(String id) {
    return Mono.empty();
  }
}
