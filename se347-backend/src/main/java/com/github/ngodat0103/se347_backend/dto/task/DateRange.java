package com.github.ngodat0103.se347_backend.dto.task;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DateRange {
  private Instant start;
  private Instant end;
}
