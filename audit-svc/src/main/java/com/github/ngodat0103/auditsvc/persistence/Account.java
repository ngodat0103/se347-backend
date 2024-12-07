package com.github.ngodat0103.auditsvc.persistence;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;
import java.util.Locale;

@Data
@Document(indexName = "account")
@Builder
public class Account {

  public enum AccountStatus {
    ACTIVE,
    INACTIVE,
    DELETED
  }

  private String accountId;

  @NotNull(message = "Nick name should not be null")
  private String nickName;

  @Email(message = "Email should be valid")
  private String email;
  private AccountStatus accountStatus;
  private boolean emailVerified;
  private String zoneInfo;
  private String pictureUrl;
  private Locale locale;
  private Instant createdDate;
  private Instant lastUpdatedDate;
}
