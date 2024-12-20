package com.github.ngodat0103.se347_backend.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ngodat0103.se347_backend.persistence.document.task.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TaskDto {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String id;

  @NotNull(message = "Name is required")
  private String name;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private TaskStatus status;

  @Null private String assigneeId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String projectId;

  //    private int position;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String workspaceId;

  private String description;
}
