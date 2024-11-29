package com.github.ngodat0103.usersvc.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

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
