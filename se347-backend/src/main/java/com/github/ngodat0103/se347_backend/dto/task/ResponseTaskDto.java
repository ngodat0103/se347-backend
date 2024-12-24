package com.github.ngodat0103.se347_backend.dto.task;

import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import com.github.ngodat0103.se347_backend.dto.user.UserDto;
import com.github.ngodat0103.se347_backend.persistence.document.task.TaskStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class ResponseTaskDto {
  private String id;

  @NotNull(message = "Name is required")
  private String name;

  @NotNull(message = "Status is required")
  private TaskStatus status;

  private Instant dueDate;

  @Setter private ProjectDto project;
  @Setter private UserDto assignee;
  //    private int position;
  private String workspaceId;
  private String description;
}
