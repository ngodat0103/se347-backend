package com.github.ngodat0103.usersvc.controller;

import com.github.ngodat0103.usersvc.dto.CredentialDto;
import com.github.ngodat0103.usersvc.service.UserService;
import com.nimbusds.jose.jwk.JWK;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  public Mono<String> verifyEmail(@RequestParam String code) {
    return Mono.just("Not implemented yet");
  }
}
