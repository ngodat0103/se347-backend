package com.github.ngodat0103.usersvc.persistence.document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document
@Builder
public class Account {

  public enum AccountStatus {
    ACTIVE,
    INACTIVE,
    DELETED
  }

  @MongoId private String accountId;

  @NotNull(message = "Nick name should not be null")
  private String nickName;

  @Email(message = "Email should be valid")
  @Indexed(unique = true, name = "idx_email")
  private String email;

  @Size(min = 8, message = "Password should have at least 8 characters")
  private String password;

  private AccountStatus accountStatus;
  private boolean emailVerified;
  private String zoneInfo;
  private String pictureUrl;
  private Locale locale;
  private LocalDateTime createdDate;
  private LocalDateTime lastUpdatedDate;
}
