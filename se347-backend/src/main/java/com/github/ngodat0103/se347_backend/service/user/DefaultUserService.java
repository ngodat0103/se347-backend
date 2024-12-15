package com.github.ngodat0103.se347_backend.service.user;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.dto.account.UserDto;
import com.github.ngodat0103.se347_backend.dto.mapper.UserMapper;
import com.github.ngodat0103.se347_backend.dto.topic.KeyTopic;
import com.github.ngodat0103.se347_backend.dto.topic.ValueTopicRegisteredUser;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.NotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import com.github.ngodat0103.se347_backend.persistence.document.user.UserStatus;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
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
public class DefaultUserService implements UserService {
  private static final String EMAIL_ALREADY_VERIFIED = "Email already verified";
  private static final String EMAIL = "email";
  private static final String USER = "User";
  private UserRepository userRepository;
  private UserMapper userMapper;
  private final KafkaTemplate<KeyTopic, ValueTopicRegisteredUser> kafkaTemplate;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserDto create(UserDto userDto) {
    var account = userMapper.toDocument(userDto);
    account.setPassword(passwordEncoder.encode(userDto.getPassword()));
    account.setUserStatus(UserStatus.ACTIVE);
    account.setEmailVerified(false);
    Instant instantNow = Instant.now();
    account.setCreatedDate(instantNow);
    account.setLastUpdatedDate(instantNow);
    if (userRepository.existsByEmail(userDto.getEmail())) {
      throw new ConflictException("Email already exists", ConflictException.Type.ALREADY_EXISTS);
    }
    account = userRepository.save(account);
    log.info("Successfully save account with email {}", account.getEmail());
    return userMapper.toDto(account);
  }

  @Override
  @Transactional
  public UserDto create(UserDto userDto, HttpServletRequest request) {
    return create(userDto);
  }

  @Override
  public UserDto update(UserDto userDto) {
    return null;
  }

  @Override
  public UserDto getMe() {
    String accountId = getUserIdFromAuthentication();
    User user =
        userRepository
            .findById(accountId)
            .orElseThrow(() -> new NotFoundException("Account not exists"));
    return userMapper.toDto(user);
  }

  @Override
  public String delete(String id) {
    return null;
  }
}
