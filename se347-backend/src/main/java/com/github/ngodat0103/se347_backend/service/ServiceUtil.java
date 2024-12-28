package com.github.ngodat0103.se347_backend.service;

import com.github.ngodat0103.se347_backend.dto.task.DateRange;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

public class ServiceUtil {
  private ServiceUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static DateRange getDateRangeForCurrentMonth() {
    YearMonth now = YearMonth.now();
    Instant start = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = now.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
    return new DateRange(start, end);
  }

  public static DateRange getDateRangeForLastMonth() {
    YearMonth now = YearMonth.now().minusMonths(1);
    Instant start = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = now.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
    return new DateRange(start, end);
  }
}
