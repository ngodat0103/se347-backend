package com.github.ngodat0103.usersvc.dto.topic;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class TopicRegisteredUser {
  public enum Action {
    REGISTER,
    NEW_USER,
    RESET_PASSWORD,
    RESEND_EMAIL_VERIFICATION,
    DATA_UPDATE
  }

  @NotNull
  private final String accountId;
  @Email
  private final String email;
  @NotNull
  private final LocalDateTime createdDate;


  @Setter
  private Action action;
  @Setter
  private Map<String,Object> additionalProperties;
}
