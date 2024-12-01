package com.github.ngodat0103.usersvc.persistence.document;

import lombok.*;
import org.testcontainers.shaded.com.google.errorprone.annotations.NoAllocation;

import java.time.Instant;

@NoArgsConstructor
@Data
public class BaseDocument {
   private Instant createdDate;
    private Instant lastUpdatedDate;
}
