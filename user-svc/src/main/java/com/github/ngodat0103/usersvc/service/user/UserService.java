package com.github.ngodat0103.usersvc.service.user;

import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import reactor.core.publisher.Mono;

public interface UserService {

  Mono<AccountDto> create(AccountDto accountDto, ServerHttpRequest request);

  Mono<AccountDto> get(String id);

  Mono<AccountDto> getMe();

  Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto);

  Mono<Void> logout(Authentication authentication);

  Mono<String> verifyEmail(String code);

  Mono<String> resendEmailVerification(ServerHttpRequest request);
}
