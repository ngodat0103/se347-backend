package com.github.ngodat0103.usersvc.service.email;

import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public interface EmailService {
  Mono<String> verifyEmail(String code);

  Mono<Void> resendEmailVerification(AccountDto accountDto, HttpHeaders forwardedHeaders);

  Mono<Void> emailNewUser(AccountDto accountDto, HttpHeaders forwardedHeaders);
}
