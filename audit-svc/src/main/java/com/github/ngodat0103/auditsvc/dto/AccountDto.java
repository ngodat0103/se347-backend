package com.github.ngodat0103.auditsvc.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;
import java.util.Locale;

@Builder
@Getter
@Document(indexName = "account")
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
