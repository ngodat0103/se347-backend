package com.github.ngodat0103.se347_backend.service.user;

import com.github.ngodat0103.se347_backend.dto.user.UpdateUserDto;
import com.github.ngodat0103.se347_backend.dto.user.UserDto;
import com.github.ngodat0103.se347_backend.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import org.springframework.http.MediaType;

public interface UserService extends BaseService<UserDto> {
  UserDto create(UserDto userDto);

  UserDto create(UserDto userDto, HttpServletRequest request);

  String updateAvatar(InputStream avatarImage, MediaType mediaType);

  UserDto update(UpdateUserDto updateUserDto);

  UserDto getMe();
}
