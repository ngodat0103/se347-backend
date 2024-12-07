package com.github.ngodat0103.usersvc.controller.service;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;

import com.github.javafaker.Faker;
import com.github.javafaker.Internet;
import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.email.EmailService;
import com.github.ngodat0103.usersvc.service.user.DefaultUserService;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class DefaultUserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private EmailService emailService;

  @InjectMocks private DefaultUserService defaultUserService;

  @Mock private ServerHttpRequest serverHttpRequest;

  @Mock private HttpHeaders httpHeaders;
  @Mock private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  private AccountDto fakeaccountDto;
  private Account mockResponseAccount;

  @BeforeEach
  void setUp() {
    Faker faker = new Faker();
    fakeaccountDto =
        AccountDto.builder()
            .email(faker.internet().emailAddress())
            .password(faker.internet().password())
            .nickName(faker.name().username())
            .build();
    mockResponseAccount =
        Account.builder()
            .email(fakeaccountDto.getEmail())
            .password(fakeaccountDto.getPassword())
            .nickName(fakeaccountDto.getNickName())
            .emailVerified(false)
            .accountId("123219783129873")
            .build();

    this.httpHeaders =
        HttpHeaders.readOnlyHttpHeaders(
            new HttpHeaders() {
              {
                Internet internet = faker.internet();
                add("X-Forwarded-Proto", internet.domainName());
                add("X-Forwarded-Host", internet.domainName());
                add("X-Forwarded-For", internet.ipV4Address());
              }
            });
  }

  @Test
  void givenNotExistsWhenCreateAccountThenReturnSuccessful() {
    when(emailService.emailNewUser(any(AccountDto.class), any(HttpHeaders.class)))
        .thenReturn(Mono.empty());

    Instant now = Instant.now();
    mockResponseAccount.setCreatedDate(now);
    mockResponseAccount.setLastUpdatedDate(now);
    given(userRepository.save(any(Account.class))).willReturn(Mono.just(mockResponseAccount));
    given(serverHttpRequest.getHeaders()).willReturn(this.httpHeaders);
    StepVerifier.create(defaultUserService.create(fakeaccountDto, serverHttpRequest))
        .expectNextMatches(
            responseAccountDto -> {
              Assertions.assertNotNull(responseAccountDto.getAccountId());
              Assertions.assertNotNull(responseAccountDto.getCreatedDate());
              Assertions.assertNotNull(responseAccountDto.getLastUpdatedDate());
              Assertions.assertEquals(fakeaccountDto.getEmail(), responseAccountDto.getEmail());
              Assertions.assertEquals(
                  fakeaccountDto.getNickName(), responseAccountDto.getNickName());
              Assertions.assertNull(responseAccountDto.getPassword());
              return true;
            })
        .verifyComplete();
  }

  @Test
  void testCreateAccountErrorHandling() {}
}
