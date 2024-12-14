package com.github.ngodat0103.usersvc.controller;

import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import com.github.ngodat0103.usersvc.service.user.UserService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

  private final UserService userService;

  @GetMapping(path = "/isValidJwt")
  @SecurityRequirement(name = "bearerAuth")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("isAuthenticated()")
  public Mono<Map<String, Object>> isValidJwt(JwtAuthenticationToken jwtAuthenticationToken) {
    Map<String, Object> claims = jwtAuthenticationToken.getTokenAttributes();
    return Mono.just(claims);
  }

  @GetMapping(path = "/jwk")
  public Mono<JWK> getJwk(RSAKey key){
    RsaPublicKey
    key.getPublicExponent()
            key.getModulus()

  }


  @PostMapping(path = "/login")
  public Mono<OAuth2AccessTokenResponse> login(@RequestBody @Valid CredentialDto credentialDto) {
    return userService.login(credentialDto);
  }

  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("isAuthenticated()")
  @GetMapping(path = "/logout")
  public Mono<String> logout(JwtAuthenticationToken jwtAuthenticationToken) {
    return this.userService.logout(jwtAuthenticationToken).then(Mono.just("Logout successfully"));
  }

  @GetMapping(path = "/verify-email")
  public Mono<String> verifyEmail(@RequestParam String code) {
    return userService.verifyEmail(code);
  }

  @PreAuthorize("isAuthenticated()")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping(path = "/resend-email")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<String> resendEmailVerification(ServerHttpRequest request) {
    return userService.resendEmailVerification(request);
  }
}
