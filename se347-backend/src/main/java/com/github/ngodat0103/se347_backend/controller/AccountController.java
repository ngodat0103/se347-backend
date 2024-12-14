package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.account.AccountDto;
import com.github.ngodat0103.se347_backend.service.account.AccountService;
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
public class AccountController {
  private AccountService accountService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AccountDto createUser(
      @Valid @RequestBody AccountDto accountDto, HttpServletRequest request) {
    return accountService.create(accountDto, request);
  }

  @PreAuthorize("isAuthenticated()")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping(path = "/me")
  public AccountDto getMe() {
    return accountService.getMe();
  }
}
