package com.github.ngodat0103.usersvc.service.auth;

import com.github.ngodat0103.usersvc.dto.account.CredentialDto;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import java.time.Duration;
import java.time.Instant;

import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
@Service
public class JwtAuthService implements AuthService {
  private static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
  private static final String USER_SVC = "user-svc";
  private final JwtEncoder jwtEncoder;
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
  private final UserRepository userRepository;
  PasswordEncoder passwordEncoder;
  private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";

  @Override
  public Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto) {
    return userRepository
            .findByEmail(credentialDto.getEmail())
            .filter(account -> passwordEncoder.matches(credentialDto.getPassword(), account.getPassword()))
            .switchIfEmpty(Mono.error(new BadCredentialsException(INVALID_EMAIL_OR_PASSWORD)))
            .doOnSuccess(account -> log.info("User {} logged in", account.getAccountId()))
            .map(this::createAccessTokenResponse);
  }

  @Override
  public Mono<Void> logout(Authentication authentication) {
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
      return Mono.error(
          new IllegalArgumentException("Authentication must be JwtAuthenticationToken"));
    }
    Jwt jwt = jwtAuthenticationToken.getToken();
    String accessToken = jwt.getTokenValue();
    Instant expireAt = jwt.getExpiresAt();
    return reactiveRedisTemplate
        .opsForValue()
        .set(
            "access_token_blacklist:" + accessToken,
            "This user have logout",
            Duration.between(Instant.now(), expireAt))
        .doOnSuccess(aVoid -> log.info("User logged out,blacklist token: {}", accessToken))
        .then();
  }

  @Override
  public OAuth2AccessTokenResponse createAccessTokenResponse(Account account) {
    Instant now = Instant.now();
    Instant expireAt = now.plusSeconds(ACCESS_TOKEN_DURATION.getSeconds());
    JwtClaimsSet jwtClaimsSet =
        JwtClaimsSet.builder()
            .subject(account.getAccountId())
            .issuedAt(now)
            .expiresAt(expireAt)
            .issuer(USER_SVC)
            .build();
    Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet));
    return OAuth2AccessTokenResponse.withToken(jwt.getTokenValue())
        .tokenType(OAuth2AccessToken.TokenType.BEARER)
        .expiresIn(expireAt.minusSeconds(now.getEpochSecond()).getEpochSecond())
        .build();
  }
}
