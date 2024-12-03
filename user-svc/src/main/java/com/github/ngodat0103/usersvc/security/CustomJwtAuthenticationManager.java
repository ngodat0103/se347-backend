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

  //   public CustomJwtAuthenticationManager(ReactiveJwtDecoder
  // jwtDecoder,ReactiveRedisTemplate<String,String> reactiveRedisTemplate) {
  //        this.jwtDecoder = jwtDecoder;
  //        this.reactiveRedisTemplate = reactiveRedisTemplate;
  //    }

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    // @formatter:off
    return Mono.justOrEmpty(authentication)
        .filter(BearerTokenAuthenticationToken.class::isInstance)
        .cast(BearerTokenAuthenticationToken.class)
        .map(BearerTokenAuthenticationToken::getToken)
        .flatMap(this.jwtDecoder::decode)
        .flatMap(
            jwt ->
                reactiveRedisTemplate
                    .opsForValue()
                    .get("access_token_blacklist:" + jwt.getTokenValue())
                    .flatMap(
                        token -> {
                          if (token != null) {
                              log.info("Token is in blacklist");
                            return Mono.error(new BadJwtException("Token is black list"));
                          }
                          return Mono.just(jwt);
                        })
                    .thenReturn(jwt))
        .flatMap(
            jwt -> {
              log.info("jwt: {}", jwt);
              return jwtAuthenticationConverter.convert(jwt);
            })
        .cast(Authentication.class)
        .onErrorMap(JwtException.class, this::onError);
    // @formatter:on
  }

  private AuthenticationException onError(JwtException ex) {
    if (ex instanceof BadJwtException) {
      return new InvalidBearerTokenException(ex.getMessage(), ex);
    }
    return new AuthenticationServiceException(ex.getMessage(), ex);
  }
}
