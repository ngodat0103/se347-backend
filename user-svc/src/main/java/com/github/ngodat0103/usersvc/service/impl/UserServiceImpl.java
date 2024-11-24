package com.github.ngodat0103.usersvc.service.impl;
import static com.github.ngodat0103.usersvc.exception.Util.*;
import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.dto.CredentialDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.topic.TopicRegisteredUser;
import com.github.ngodat0103.usersvc.exception.ConflictException;
import com.github.ngodat0103.usersvc.exception.InvalidEmailCodeException;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.ServiceProducer;
import com.github.ngodat0103.usersvc.service.UserService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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
    private final ReactiveRedisTemplate<String, TopicRegisteredUser> redisTemplate;

    @Override
    public Mono<AccountDto> getMe() {
        return getUserIdFromAuthentication()
                .flatMap(userRepository::findById)
                .map(userMapper::toDto);
    }

    @Override
    public Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto) {
        return userRepository
                .findByEmail(credentialDto.getEmail())
                .filter(account -> passwordEncoder.matches(credentialDto.getPassword(), account.getPassword()))
                .switchIfEmpty(Mono.error(new BadCredentialsException(INVALID_EMAIL_OR_PASSWORD)))
                .map(this::createAccessTokenResponse);
    }

    private OAuth2AccessTokenResponse createAccessTokenResponse(Account account) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(Duration.ofMinutes(5).getSeconds());
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
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
        return redisTemplate.opsForValue().get(code)
                .switchIfEmpty(Mono.defer(() -> Mono.error(InvalidEmailCodeException::new)))
                .flatMap(topicRegisteredUser -> userRepository.findById(topicRegisteredUser.getAccountId()))
                .doOnNext(account -> account.setEmailVerified(true))
                .flatMap(userRepository::save)
                .map(account -> {
                    log.info("Email verified for user {}", account.getAccountId());
                    return "Email verified";
                });

    }

    @Override
    public Mono<String> resendEmailVerification() {
        return getUserIdFromAuthentication()
                .flatMap(userRepository::findById)
                .doOnNext(account -> {
                    if (account.isEmailVerified()) {
                        throw new ConflictException(EMAIL_ALREADY_VERIFIED, ConflictException.Type.ALREADY_VERIFIED);
                    }
                })
                .flatMap(account -> {
                    String verifyEmailCode = generateVerifyEmailCode();
                    TopicRegisteredUser topicRegisteredUser = userMapper.toTopicRegisteredUse(account);
                    topicRegisteredUser.setAction(TopicRegisteredUser.Action.RESEND_EMAIL_VERIFICATION);
                    topicRegisteredUser.setVerifyEmailCode(verifyEmailCode);
                    log.info("Sending resend email verification to kafka: {}", topicRegisteredUser);
                    return redisTemplate.opsForValue().set(verifyEmailCode, topicRegisteredUser)
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
        account.setCreatedDate(LocalDateTime.now());
        account.setLastUpdatedDate(LocalDateTime.now());
        return userRepository.save(account)
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
        TopicRegisteredUser topicRegisteredUser = userMapper.toTopicRegisteredUse(account);
        topicRegisteredUser.setAction(TopicRegisteredUser.Action.NEW_USER);
        String verifyEmailCode = generateVerifyEmailCode();
        topicRegisteredUser.setVerifyEmailCode(verifyEmailCode);
        log.info("Sending new registered user to kafka: {}", topicRegisteredUser);
        serviceProducer.sendRegisteredUser(topicRegisteredUser);
        return redisTemplate.opsForValue().set(verifyEmailCode, topicRegisteredUser)
                .thenReturn(account)
                .map(userMapper::toDto);
    }

    private static String generateVerifyEmailCode() {
        byte[] randomBytes = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);

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