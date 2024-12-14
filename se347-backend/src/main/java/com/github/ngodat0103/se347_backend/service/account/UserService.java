package com.github.ngodat0103.se347_backend.service.account;

import com.github.ngodat0103.se347_backend.dto.account.UserDto;
import com.github.ngodat0103.se347_backend.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService extends BaseService<UserDto> {
  UserDto create(UserDto userDto);

  UserDto create(UserDto userDto, HttpServletRequest request);

  UserDto update(UserDto userDto);

  UserDto getMe();
}
