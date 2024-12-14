package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.account.UserDto;
import com.github.ngodat0103.se347_backend.service.account.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/users")
public class UserController {
  private UserService userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto createUser(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
    return userService.create(userDto, request);
  }

  @PreAuthorize("isAuthenticated()")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping(path = "/me")
  public UserDto getMe() {
    return userService.getMe();
  }
}
