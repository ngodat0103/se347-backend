package com.github.ngodat0103.se347_backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CredentialDto {
  @Email(message = "Email should be valid")
  private String email;

  @NotNull(message = "Password should not be null")
  private String password;
}
