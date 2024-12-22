package com.github.ngodat0103.se347_backend.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ngodat0103.se347_backend.persistence.document.task.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreateTaskDto {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String id;

  @NotNull(message = "Name is required")
  private String name;

  @NotNull(message = "Status is required")
  private TaskStatus status;

  private Instant dueDate;
  @Null private String assigneeId;

  //    private int position;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String workspaceId;

  private String description;
}
