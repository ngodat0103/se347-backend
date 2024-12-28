package com.github.ngodat0103.se347_backend.dto.project;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectAnalyticsDto {
  private int taskCount;
  private int taskDifference;
  private int assignedTaskCount;
  private int assignedTaskDifference;
  private int completedTaskCount;
  private int completedTaskDifference;
  private int inCompletedTaskCount;
  private int inCompletedTaskDifference;
  private int overdueTaskCount;
  private int overdueTaskDifference;
}
