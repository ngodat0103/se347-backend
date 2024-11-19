package com.github.ngodat0103.usersvc.service.impl;

import static com.github.ngodat0103.usersvc.exception.Util.*;

import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.dto.CredentialDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
  private UserRepository userRepository;
  private UserMapper userMapper;
  private PasswordEncoder passwordEncoder;
  private JwtEncoder jwtEncoder;

  @Override
  public Mono<AccountDto> getMe() {
    return getUserIdFromAuthentication().flatMap(userRepository::findById).map(userMapper::toDto);
  }

  @Override
  public Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto) {
    String email = credentialDto.getEmail();
    String password = credentialDto.getPassword();
    return userRepository
        .findByEmail(email)
        .filter(account -> passwordEncoder.matches(password, account.getPassword()))
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid email or password"))))
        .map(
            account -> {
              Instant now = Instant.now();
              Instant expireAt = now.plusSeconds(Duration.ofMinutes(5).getSeconds());
              JwtClaimsSet jwtClaimsSet =
                  JwtClaimsSet.builder()
                      .subject(account.getAccountId())
                      .issuedAt(now)
                      .expiresAt(expireAt)
                      .issuer("user-svc")
                      .build();
              JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(jwtClaimsSet);
              Jwt jwt = jwtEncoder.encode(jwtEncoderParameters);
              return OAuth2AccessTokenResponse.withToken(jwt.getTokenValue())
                  .tokenType(OAuth2AccessToken.TokenType.BEARER)
                  .expiresIn(expireAt.minusSeconds(now.getEpochSecond()).getEpochSecond())
                  .build();
            });
  }

  @Override
  public Mono<AccountDto> create(AccountDto accountDto) {
    Account account = userMapper.toDocument(accountDto);
    account.setAccountStatus(Account.AccountStatus.ACTIVE);
    account.setEmailVerified(false);
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    account.setCreatedDate(LocalDateTime.now());
    account.setLastUpdatedDate(LocalDateTime.now());
    return userRepository
        .save(account)
        .doOnError(
            e -> {
              if (e.getMessage().contains("idx_email")) {
                throw createConflictException(log, "User", "email", account.getEmail());
              }
              log.error("Not expected exception", e);
            })
        .map(userMapper::toDto);
  }

  @Override
  public Mono<AccountDto> update(AccountDto accountDto) {
    return null;
  }

  @Override
  public Mono<AccountDto> delete(AccountDto accountDto) {
    return null;
  }

  @Override
  public Mono<AccountDto> get(AccountDto accountDto) {
    return null;
  }
}
