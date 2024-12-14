package com.github.ngodat0103.se347_backend.persistence.document;

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BaseDocument {
  private Instant createdDate;
  private Instant lastUpdatedDate;
}
