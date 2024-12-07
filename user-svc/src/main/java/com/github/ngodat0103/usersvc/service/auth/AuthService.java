package com.github.ngodat0103.usersvc.service.auth;

import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import reactor.core.publisher.Mono;

public interface AuthService {
  Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto);

  Mono<Void> logout(Authentication jwtAuthenticationToken);

  OAuth2AccessTokenResponse createAccessTokenResponse(Account account);
}
