package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.dto.CredentialDto;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import reactor.core.publisher.Mono;

public interface UserService extends BaseInterface<AccountDto> {

  Mono<AccountDto> getMe();

  Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto);
  Mono<String> verifyEmail(String code);
  Mono<String> resendEmailVerification();
}
