package com.github.ngodat0103.usersvc.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailDto {
    private String accountId;
  private String emailVerificationCode;
  private String emailVerificationEndpoint;
  private String email;
}
