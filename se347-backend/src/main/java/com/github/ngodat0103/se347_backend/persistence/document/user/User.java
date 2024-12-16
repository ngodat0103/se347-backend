package com.github.ngodat0103.se347_backend.persistence.document.user;

import com.github.ngodat0103.se347_backend.persistence.document.BaseDocument;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "users")
@Builder
public class User extends BaseDocument {

  @MongoId private String userId;

  @NotNull(message = "Nick name should not be null")
  private String nickName;

  @Email(message = "Email should be valid")
  @Indexed(unique = true, name = "idx_email")
  private String email;

  @Size(min = 8, message = "Password should have at least 8 characters")
  private String password;

  private UserStatus userStatus;
  private boolean emailVerified;
  private String zoneInfo;
  private String imageUrl;
  private Locale locale;
}
