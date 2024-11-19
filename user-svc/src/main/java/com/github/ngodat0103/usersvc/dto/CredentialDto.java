package com.github.ngodat0103.usersvc.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CredentialDto {
  private String email;
  private String password;
}
