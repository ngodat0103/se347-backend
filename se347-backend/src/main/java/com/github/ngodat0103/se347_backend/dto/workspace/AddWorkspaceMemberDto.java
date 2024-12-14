package com.github.ngodat0103.se347_backend.dto.workspace;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddWorkspaceMemberDto {
  @Email @NotNull private String email;
}
