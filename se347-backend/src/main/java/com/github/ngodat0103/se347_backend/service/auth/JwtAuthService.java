package com.github.ngodat0103.se347_backend.service.auth;

import com.github.ngodat0103.se347_backend.dto.account.CredentialDto;
import com.github.ngodat0103.se347_backend.exception.NotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import com.github.ngodat0103.se347_backend.persistence.document.user.UserStatus;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class JwtAuthService implements AuthService {
  private static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(7); // Dev only
  private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public OAuth2AccessTokenResponse login(CredentialDto credentialDto) {
    User user =
        userRepository
            .findByEmailAndUserStatus(credentialDto.getEmail(), UserStatus.ACTIVE)
            .orElseThrow(() -> new NotFoundException("User with email is not exists"));
    if (!passwordEncoder.matches(credentialDto.getPassword(), user.getPassword())) {
      throw new BadCredentialsException(INVALID_EMAIL_OR_PASSWORD);
    }
    log.info("User {} logged in", user.getEmail());
    return this.createAccessTokenResponse(user);
  }

  @Override
  public String logout(Authentication authentication) {
    if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
      String token = jwtAuthenticationToken.getToken().getTokenValue();
      redisTemplate.opsForValue().set(token, "blacklist");
      log.info("Black list token: {}", token);
      return "Logout successfully";
    }
    throw new UnsupportedOperationException("");
  }

  @Override
  public String verifyEmail(String code) {
    throw new NotImplementedException("Not implemented yet");
  }

  @Override
  public String resendEmailVerification(HttpServletRequest request) {
    throw new NotImplementedException("Not implemented yet");
  }

  public OAuth2AccessTokenResponse createAccessTokenResponse(User user) {
    Instant now = Instant.now();
    Instant expireAt = now.plusSeconds(ACCESS_TOKEN_DURATION.getSeconds());
    JwtClaimsSet jwtClaimsSet =
        JwtClaimsSet.builder()
            .subject(user.getAccountId().toString())
            .issuedAt(now)
            .expiresAt(expireAt)
            .issuer("se347-backend")
            .build();
    Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet));
    return OAuth2AccessTokenResponse.withToken(jwt.getTokenValue())
        .tokenType(OAuth2AccessToken.TokenType.BEARER)
        .expiresIn(expireAt.minusSeconds(now.getEpochSecond()).getEpochSecond())
        .build();
  }
}
