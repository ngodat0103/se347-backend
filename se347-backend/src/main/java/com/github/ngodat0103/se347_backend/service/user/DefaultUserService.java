package com.github.ngodat0103.se347_backend.service.user;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.dto.mapper.UserMapper;
import com.github.ngodat0103.se347_backend.dto.topic.KeyTopic;
import com.github.ngodat0103.se347_backend.dto.topic.ValueTopicRegisteredUser;
import com.github.ngodat0103.se347_backend.dto.user.UpdateUserDto;
import com.github.ngodat0103.se347_backend.dto.user.UserDto;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.notfound.UserNotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import com.github.ngodat0103.se347_backend.persistence.document.user.UserStatus;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
import com.github.ngodat0103.se347_backend.service.minio.MinioService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Slf4j
public class DefaultUserService implements UserService {
  private UserRepository userRepository;
  private UserMapper userMapper;
  private final KafkaTemplate<KeyTopic, ValueTopicRegisteredUser> kafkaTemplate;
  private final PasswordEncoder passwordEncoder;
  private final MinioService minioService;

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
  public String updateAvatar(InputStream avatarImage, MediaType mediaType) {
    String objectName = "users/" + getUserIdFromAuthentication() + "/avatar";
    try {
      User user =
          userRepository
              .findById(getUserIdFromAuthentication())
              .orElseThrow(() -> new UserNotFoundException("id", getUserIdFromAuthentication()));
      String publicUrl =
          minioService.uploadFile(objectName, avatarImage, avatarImage.available(), mediaType);
      user.setImageUrl(publicUrl);
      userRepository.save(user);
      return publicUrl;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public UserDto update(UpdateUserDto updateUserDto) {
    String callUserId = getUserIdFromAuthentication();
    User user =
        userRepository
            .findById(callUserId)
            .orElseThrow(() -> new UserNotFoundException("id", callUserId));
    user.setNickName(updateUserDto.getNickName());
    user = userRepository.save(user);
    return userMapper.toDto(user);
  }

  @Override
  public UserDto update(String userId, UserDto userDto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UserDto getMe() {
    String userId = getUserIdFromAuthentication();
    User user =
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("id", userId));
    return userMapper.toDto(user);
  }

  @Override
  public String delete(String id) {
    return null;
  }
}
