package com.github.ngodat0103.usersvc.persistence.document;

import java.time.Instant;
import lombok.*;

@NoArgsConstructor
@Data
public class BaseDocument {
  private Instant createdDate;
  private Instant lastUpdatedDate;
}
