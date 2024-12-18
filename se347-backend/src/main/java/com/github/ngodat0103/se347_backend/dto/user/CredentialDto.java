package com.github.ngodat0103.se347_backend.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CredentialDto {
  private String email;
  private String password;
}
