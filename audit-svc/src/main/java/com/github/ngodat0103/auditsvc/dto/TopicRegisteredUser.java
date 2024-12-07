package com.github.ngodat0103.auditsvc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopicRegisteredUser {
  public enum Action {
    NEW_USER,
    RESET_PASSWORD,
    RESEND_EMAIL_VERIFICATION,
    DATA_UPDATE
  }

  @NotNull private  Instant createdDate;
  private  Action action;
  private Map<String, Object> additionalProperties;
  private static final int VERSION = 1; // for schema evolution
}
