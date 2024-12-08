package com.github.ngodat0103.usersvc.service.user;

import com.github.ngodat0103.usersvc.dto.account.AccountDto;
import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import java.util.Set;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import reactor.core.publisher.Mono;

public interface UserService {

  Mono<AccountDto> create(AccountDto accountDto, ServerHttpRequest request);

  Mono<AccountDto> getById(String id);

  Mono<AccountDto> getByEmail(String email);

  Mono<Set<AccountDto>> getByNickName(String nickName);

  Mono<AccountDto> getMe();

  Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto);

  Mono<Void> logout(Authentication authentication);

  Mono<String> verifyEmail(String code);

  Mono<String> resendEmailVerification(ServerHttpRequest request);
}
