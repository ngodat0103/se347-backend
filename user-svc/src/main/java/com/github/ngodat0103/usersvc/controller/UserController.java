package com.github.ngodat0103.usersvc.controller;

import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.exception.ConflictException;
import com.github.ngodat0103.usersvc.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
  private UserService userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<AccountDto> createUser(@Valid @RequestBody AccountDto accountDto)
      throws ConflictException {
    return userService.create(accountDto);
  }

  //  @GetMapping(path = "/{id}")
  //  public UserDto getUser(@PathVariable(value = "id") String id) {
  //    return userService.findById(id);
  //  }

  @PreAuthorize("isAuthenticated()")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping(path = "/me")
  public Mono<AccountDto> getMe() {
    return userService.getMe();
  }
}
