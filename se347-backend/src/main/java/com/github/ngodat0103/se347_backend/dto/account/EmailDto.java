package com.github.ngodat0103.se347_backend.dto.account;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class EmailDto {
  @NonNull private String accountId;
  @NonNull private String emailVerificationCode;
  @NonNull private String emailVerificationEndpoint;

  @NonNull
  @Email(message = "Invalid email address")
  private String email;
}
