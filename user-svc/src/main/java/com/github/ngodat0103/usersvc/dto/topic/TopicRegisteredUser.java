package com.github.ngodat0103.usersvc.dto.topic;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopicRegisteredUser {
  @NotNull private final String accountId;
  @Email private final String email;
  @NotNull private final LocalDateTime createdDate;
}
