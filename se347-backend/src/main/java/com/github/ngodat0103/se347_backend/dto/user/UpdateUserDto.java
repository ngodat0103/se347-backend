package com.github.ngodat0103.se347_backend.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserDto {
  @NotNull(message = "nickName is required")
  private String nickName;
}
