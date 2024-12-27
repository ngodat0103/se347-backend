package com.github.ngodat0103.se347_backend.dto.task;

import com.github.ngodat0103.se347_backend.persistence.document.task.TaskStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateTaskDto {
  @NotNull(message = "Task name is required")
  private String name;

  @NotNull(message = "Task status is required")
  private TaskStatus status;

  private String assigneeId;

  @NotNull(message = "Task position is required")
  private int position;

  private Instant dueDate;

  private String description;
}
