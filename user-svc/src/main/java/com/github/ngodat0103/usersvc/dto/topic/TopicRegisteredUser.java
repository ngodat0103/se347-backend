package com.github.ngodat0103.usersvc.dto.topic;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopicRegisteredUser {
  public enum Action {
    NEW_USER,
    RESET_PASSWORD,
    RESEND_EMAIL_VERIFICATION,
    DATA_UPDATE
  }

  @NotNull private final Instant createdDate;
  private final Action action;
  private final Map<String, Object> additionalProperties;
  private static final int VERSION = 1; // for schema evolution
}
