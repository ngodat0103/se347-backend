package com.github.ngodat0103.usersvc.controller;

import com.github.ngodat0103.usersvc.dto.CredentialDto;
import com.github.ngodat0103.usersvc.service.UserService;
import com.nimbusds.jose.jwk.JWK;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

  private final JWK jwk;
  private final UserService userService;

  @GetMapping(path = "/jwk", produces = "application/json")
  public String getJwk() {
    return jwk.toString();
  }

  @PostMapping(path = "/login")
  public Mono<OAuth2AccessTokenResponse> login(@RequestBody @Valid CredentialDto credentialDto) {
    return userService.login(credentialDto);
  }

  @GetMapping(path = "/verify-email")
  public Mono<String> verifyEmail(@RequestParam String code) {
    return  userService.verifyEmail(code);
  }

  @PreAuthorize("isAuthenticated()")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping(path = "/resend-email")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<String> resendEmailVerification() {
    return userService.resendEmailVerification();
  }
}
