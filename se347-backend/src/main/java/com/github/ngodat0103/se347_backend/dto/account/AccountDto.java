package com.github.ngodat0103.se347_backend.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Locale;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDto {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String accountId;

  private String nickName;

  @NotBlank
  @Email
  @Size(max = 255)
  private String email;

  @NotBlank
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Size(min = 8, max = 255)
  private String password;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String zoneInfo;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String imageUrl;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Locale locale;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private boolean emailVerified;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant lastUpdatedDate;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant createdDate;
}
