package com.github.ngodat0103.usersvc.service.impl;

import static com.github.ngodat0103.usersvc.exception.Util.*;


import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.dto.CredentialDto;
import com.github.ngodat0103.usersvc.dto.mapper.UserMapper;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.JwtEncoder;
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
    return null;
  }

  @Override
  public Mono<OAuth2AccessTokenResponse> login(CredentialDto credentialDto) {
    return null;
  }

  @Override
  public Mono<AccountDto> create(AccountDto accountDto) {
    return null;
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
