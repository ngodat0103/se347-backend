package com.github.ngodat0103.usersvc.dto.topic;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class TopicRegisteredUser {
  public enum Action {
    REGISTER,
    NEW_USER,
    RESET_PASSWORD,
    RESEND_EMAIL_VERIFICATION,
    DATA_UPDATE
  }
  private Action action;
  @NotNull
  private final String accountId;
  @Email
  private final String email;
  @NotNull
  private final LocalDateTime createdDate;
  @Null
  private String verifyEmailCode;
}
