package com.github.ngodat0103.se347_backend.service.account;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.dto.account.AccountDto;
import com.github.ngodat0103.se347_backend.dto.mapper.AccountMapper;
import com.github.ngodat0103.se347_backend.dto.topic.KeyTopic;
import com.github.ngodat0103.se347_backend.dto.topic.ValueTopicRegisteredUser;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.NotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.account.Account;
import com.github.ngodat0103.se347_backend.persistence.document.account.AccountStatus;
import com.github.ngodat0103.se347_backend.persistence.repository.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Slf4j
public class DefaultAccountService implements AccountService {
  private static final String EMAIL_ALREADY_VERIFIED = "Email already verified";
  private static final String EMAIL = "email";
  private static final String USER = "User";
  private AccountRepository accountRepository;
  private AccountMapper accountMapper;
  private final KafkaTemplate<KeyTopic, ValueTopicRegisteredUser> kafkaTemplate;
  private final PasswordEncoder passwordEncoder;

  @Override
  public AccountDto create(AccountDto accountDto) {
    var account = accountMapper.toDocument(accountDto);
    account.setPassword(passwordEncoder.encode(accountDto.getPassword()));
    account.setAccountStatus(AccountStatus.ACTIVE);
    account.setEmailVerified(false);
    Instant instantNow = Instant.now();
    account.setCreatedDate(instantNow);
    account.setLastUpdatedDate(instantNow);
    if (accountRepository.existsByEmail(accountDto.getEmail())) {
      throw new ConflictException("Email already exists", ConflictException.Type.ALREADY_EXISTS);
    }
    account = accountRepository.save(account);
    log.info("Successfully save account with email {}", account.getEmail());
    return accountMapper.toDto(account);
  }

  @Override
  @Transactional
  public AccountDto create(AccountDto accountDto, HttpServletRequest request) {
    return create(accountDto);
  }

  @Override
  public AccountDto update(AccountDto accountDto) {
    return null;
  }

  @Override
  public AccountDto getMe() {
    String accountId = getUserIdFromAuthentication();
    Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new NotFoundException("Account not exists"));
    return accountMapper.toDto(account);
  }

  @Override
  public AccountDto delete(Long id) {
    return null;
  }
}
