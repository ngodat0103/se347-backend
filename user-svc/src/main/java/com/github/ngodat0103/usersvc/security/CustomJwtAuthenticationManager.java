package com.github.ngodat0103.usersvc.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Primary
@Slf4j
@AllArgsConstructor
public class CustomJwtAuthenticationManager implements ReactiveAuthenticationManager {
  private final ReactiveJwtDecoder jwtDecoder;

  private final Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>>
      jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();

  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  private Mono<Jwt> checkBlacklist(Jwt jwt) {
    return reactiveRedisTemplate
        .opsForValue()
        .get("access_token_blacklist:" + jwt.getTokenValue())
        .flatMap(
            blacklistToken -> {
              if (blacklistToken != null) {
                log.info("Token is blacklisted: {}", jwt.getTokenValue());
                return Mono.error(new InvalidBearerTokenException("Token is blacklisted"));
              }
              return Mono.empty();
            })
        .thenReturn(jwt);
  }

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    return Mono.justOrEmpty(authentication)
        .filter(BearerTokenAuthenticationToken.class::isInstance)
        .cast(BearerTokenAuthenticationToken.class)
        .map(BearerTokenAuthenticationToken::getToken)
        .flatMap(this.jwtDecoder::decode)
        .flatMap(this::checkBlacklist) // Refactor blacklist check into a method
        .flatMap(jwtAuthenticationConverter::convert) // Refactor conversion into a method
        .cast(Authentication.class)
        .onErrorMap(JwtException.class, this::onError); // Handle JWT-specific errors
  }

  private AuthenticationException onError(JwtException ex) {
    if (ex instanceof BadJwtException) {
      return new InvalidBearerTokenException(ex.getMessage(), ex);
    }
    return new AuthenticationServiceException(ex.getMessage(), ex);
  }
}
