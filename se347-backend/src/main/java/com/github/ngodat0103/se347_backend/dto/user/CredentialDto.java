package com.github.ngodat0103.se347_backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CredentialDto {
  @Email(message = "Email should be valid")
  @NotNull(message = "Email should not be null")
  private String email;

  @NotNull(message = "Password should not be null")
  @Min(value = 8, message = "Password should have at least 8 characters")
  private String password;
}
