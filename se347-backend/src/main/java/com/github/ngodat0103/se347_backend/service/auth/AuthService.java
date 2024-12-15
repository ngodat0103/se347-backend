package com.github.ngodat0103.se347_backend.service.auth;

import com.github.ngodat0103.se347_backend.dto.account.CredentialDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public interface AuthService {
  OAuth2AccessTokenResponse login(CredentialDto credentialDto);

  String logout(Authentication jwtAuthenticationToken);

  String verifyEmail(String code);

  String resendEmailVerification(HttpServletRequest request);
}
