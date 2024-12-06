//package com.github.ngodat0103.usersvc.controller.service;
//
//import static org.mockito.BDDMockito.*;
//import static org.mockito.Mockito.when;
//
//import com.github.javafaker.Faker;
//import com.github.ngodat0103.usersvc.dto.account.AccountDto;
//import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
//import com.github.ngodat0103.usersvc.dto.mapper.UserMapperImpl;
//import com.github.ngodat0103.usersvc.persistence.document.Account;
//import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
//import com.github.ngodat0103.usersvc.service.user.DefaultUserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//@ExtendWith(SpringExtension.class)
//public class defaultUserServiceTest {
//  private final UserMapper userMapper = new UserMapperImpl();
//
//  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//  @Mock private UserRepository userRepository;
//
//  private DefaultUserService defaultUserService; // Actual service class being tested
//
//  @Mock private ServerHttpRequest serverHttpRequest;
//
//  @Mock private HttpHeaders httpHeaders;
//
//  private AccountDto fakeaccountDto;
//  private Account fakeAccount;
//
//  @BeforeEach
//  void setUp() {
//    Faker faker = new Faker();
//    fakeaccountDto = new AccountDto();
//    fakeaccountDto.setEmail(faker.internet().safeEmailAddress());
//    fakeaccountDto.setPassword(faker.internet().password(8, 20));
//    fakeaccountDto.setNickName(faker.name().fullName());
//    fakeAccount =
//        Account.builder()
//            .email(fakeaccountDto.getEmail())
//            .password(fakeaccountDto.getPassword())
//            .nickName(fakeaccountDto.getNickName())
//            .build();
//
//    // Mock ServerHttpRequest headers
//    when(serverHttpRequest.getHeaders()).thenReturn(httpHeaders);
//
//    this.defaultUserService = new DefaultUserService(userRepository, userMapper, passwordEncoder);
//  }
//
//  @Test
//  void givenNotExistsWhenCreateAccountThenReturnSuccessful() {
//    given(userRepository.save(any(Account.class))).willReturn(Mono.just(fakeAccount));
//    StepVerifier.create(defaultUserService.create(fakeaccountDto, serverHttpRequest))
//        .expectNext(fakeaccountDto)
//        .verifyComplete();
//  }
//
//  @Test
//  void testCreateAccountErrorHandling() {}
//}
