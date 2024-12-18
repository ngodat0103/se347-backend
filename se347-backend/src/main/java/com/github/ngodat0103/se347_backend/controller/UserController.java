package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.user.UserDto;
import com.github.ngodat0103.se347_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

  @Operation(summary = "Create User", description = "Create a new user account")
  @ApiResponse(responseCode = "201", description = "User created successfully")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto createUser(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
    return userService.create(userDto, request);
  }

  @Operation(summary = "Get Current User", description = "Retrieve current user's information")
  @ApiResponse(responseCode = "200", description = "User information retrieved successfully")
  @PreAuthorize("isAuthenticated()")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping(path = "/me")
  public UserDto getMe() {
    return userService.getMe();
  }
}
