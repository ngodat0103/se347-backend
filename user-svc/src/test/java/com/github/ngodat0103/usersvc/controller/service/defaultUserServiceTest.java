package com.github.ngodat0103.usersvc.controller.service;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;

import com.github.javafaker.Faker;
import com.github.javafaker.Internet;
import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapperImpl;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.email.EmailService;
import com.github.ngodat0103.usersvc.service.email.UserEmailService;
import com.github.ngodat0103.usersvc.service.user.DefaultUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

@ExtendWith(SpringExtension.class)
public class defaultUserServiceTest {

  @Mock private UserRepository userRepository;


  @Mock private EmailService emailService;

  @InjectMocks
  private DefaultUserService defaultUserService;

  @Mock private ServerHttpRequest serverHttpRequest;

  @Mock private HttpHeaders httpHeaders;
  @Mock private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  private AccountDto fakeaccountDto;
  private Account fakeAccount;

  @BeforeEach
  void setUp() {
    Faker faker = new Faker();
    fakeaccountDto = AccountDto.builder()
        .email(faker.internet().emailAddress())
        .password(faker.internet().password())
        .nickName(faker.name().username())
        .build();
    fakeAccount =
        Account.builder()
            .email(fakeaccountDto.getEmail())
            .password(fakeaccountDto.getPassword())
            .nickName(fakeaccountDto.getNickName())
            .build();

    // Mock ServerHttpRequest headers
    this.httpHeaders = HttpHeaders.readOnlyHttpHeaders(
        new HttpHeaders() {
          {
              Internet internet = faker.internet();
            add("X-Forwarded-Proto", internet.domainName());
            add("X-Forwarded-Host", internet.domainName());
            add("X-Forwarded-For", internet.ipV4Address());
          }
        });
//    when(httpHeaders.getFirst("X-Forwarded-Scheme")).thenReturn("http");
  }

  @Test
  void givenNotExistsWhenCreateAccountThenReturnSuccessful() {
      Mono<Void> voidMono = Mono.empty();
      when(emailService.emailNewUser(any(AccountDto.class), any(HttpHeaders.class)))
              .thenReturn(Mono.empty());
     Account mockResponse = Account.builder()
        .email(fakeaccountDto.getEmail())
        .password(fakeaccountDto.getPassword())
        .nickName(fakeaccountDto.getNickName())
             .emailVerified(false)
             .accountId("123219783129873")
        .build();
      Instant now = Instant.now();
     mockResponse.setCreatedDate(now);
        mockResponse.setLastUpdatedDate(now);
    given(userRepository.save(any(Account.class))).willReturn(Mono.just(mockResponse));
    given(serverHttpRequest.getHeaders()).willReturn(this.httpHeaders);
    StepVerifier.create(defaultUserService.create(fakeaccountDto, serverHttpRequest))
        .expectNextMatches(
            responseAccountDto -> {
                Assertions.assertNotNull(responseAccountDto.getAccountId());
                Assertions.assertNotNull(responseAccountDto.getCreatedDate());
                Assertions.assertNotNull(responseAccountDto.getLastUpdatedDate());
                Assertions.assertEquals(fakeaccountDto.getEmail(), responseAccountDto.getEmail());
                Assertions.assertEquals(fakeaccountDto.getNickName(), responseAccountDto.getNickName());
                Assertions.assertNull(responseAccountDto.getPassword());
              return true;
            })
        .verifyComplete();
  }

  @Test
  void testCreateAccountErrorHandling() {

  }
}
