package com.github.ngodat0103.usersvc.service.email;

import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.dto.account.EmailDto;
import com.github.ngodat0103.usersvc.dto.topic.Action;
import com.github.ngodat0103.usersvc.dto.topic.KeyTopic;
import com.github.ngodat0103.usersvc.dto.topic.ValueTopicRegisteredUser;
import com.github.ngodat0103.usersvc.service.kafka.DefaultKafkaService;
import com.nimbusds.jose.util.Base64URL;
import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class UserEmailService implements EmailService {

  private static final URI verifyEmailEndpoint =
      URI.create("http://localhost:5000/api/v1/auth/verify-email");
  private final DefaultKafkaService defaultKafkaService;
  private static final String TOPIC_NAME = "user-email";

  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  @Override
  public Mono<String> verifyEmail(String code) {
    return null;
  }

  @Override
  public Mono<Void> resendEmailVerification(AccountDto account, HttpHeaders forwardedHeaders) {
    EmailDto emailDto = createEmailDto(account, forwardedHeaders);
    ValueTopicRegisteredUser valueTopicRegisteredUser =
        createValueTopicRegisteredUser(emailDto, Action.RESEND_EMAIL_VERIFICATION);
    log.info("Sending resend email verification to kafka: {}", valueTopicRegisteredUser);
    KeyTopic keyTopic = new KeyTopic("account", account.getAccountId());
    defaultKafkaService.sendMessage(TOPIC_NAME, keyTopic, valueTopicRegisteredUser);
    return storeVerificationCodeInRedis(
            emailDto.getEmailVerificationCode(), emailDto.getAccountId())
        .then();
  }

  @Override
  public Mono<Void> emailNewUser(AccountDto accountDto, HttpHeaders forwardedHeaders) {
    EmailDto emailDto = createEmailDto(accountDto, forwardedHeaders);
    KeyTopic keyTopic = new KeyTopic("account", accountDto.getAccountId());
    ValueTopicRegisteredUser valueTopicRegisteredUser =
        createValueTopicRegisteredUser(emailDto, Action.INSERT);
    defaultKafkaService.sendMessage(TOPIC_NAME, keyTopic, valueTopicRegisteredUser);
    return storeVerificationCodeInRedis(
        emailDto.getEmailVerificationCode(), emailDto.getAccountId());
  }

  private static EmailDto createEmailDto(AccountDto accountDto, HttpHeaders forwardedHeaders) {
    String verifyEmailCode = generateVerifyEmailCode();
    String emailEndpointUrl =
        ForwardedHeaderUtils.adaptFromForwardedHeaders(verifyEmailEndpoint, forwardedHeaders)
            .query("code=" + verifyEmailCode)
            .build()
            .toUriString();

    EmailDto emailDto = new EmailDto();
    emailDto.setAccountId(accountDto.getAccountId());
    emailDto.setEmailVerificationCode(verifyEmailCode);
    emailDto.setEmailVerificationEndpoint(emailEndpointUrl);
    emailDto.setEmail(accountDto.getEmail());
    return emailDto;
  }

  private static ValueTopicRegisteredUser createValueTopicRegisteredUser(
      EmailDto emailDto, Action action) {
    Map<String, Object> additionalProperties = Map.of("emailDto", emailDto);
    return ValueTopicRegisteredUser.builder()
        .createdDate(LocalDateTime.now().toInstant(ZoneOffset.UTC))
        .action(action)
        .additionalProperties(additionalProperties)
        .build();
  }

  public static String generateVerifyEmailCode() {
    byte[] randomBytes = new byte[16];
    SecureRandom random = new SecureRandom();
    random.nextBytes(randomBytes);
    return Base64URL.encode(randomBytes).toString();
  }

  public Mono<Void> storeVerificationCodeInRedis(String verifyEmailCode, String accountId) {
    log.info("Storing email verification code in Redis: {}", verifyEmailCode);
    return reactiveRedisTemplate.opsForValue().set(verifyEmailCode, accountId).then();
  }
}
