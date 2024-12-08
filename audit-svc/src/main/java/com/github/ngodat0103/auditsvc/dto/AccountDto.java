package com.github.ngodat0103.auditsvc.dto;

import java.time.Instant;
import java.util.Locale;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;

@Builder
@Getter
@Document(indexName = "accounts")
public class AccountDto {
  private String accountId;

  private String nickName;

  private String email;

  private String zoneInfo;

  private String pictureUrl;

  private Locale locale;

  private boolean emailVerified;

  private Instant lastUpdatedDate;

  private Instant createdDate;
}
