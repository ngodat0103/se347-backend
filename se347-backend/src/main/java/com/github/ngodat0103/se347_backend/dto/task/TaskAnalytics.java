package com.github.ngodat0103.se347_backend.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskAnalytics {
  private int taskCount;
  private int assignedTaskCount;
  private int completedTaskCount;
  private int inCompletedTaskCount;
  private int overdueTaskCount;
}
