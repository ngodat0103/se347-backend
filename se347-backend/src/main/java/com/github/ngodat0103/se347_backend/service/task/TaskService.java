package com.github.ngodat0103.se347_backend.service.task;

import com.github.ngodat0103.se347_backend.dto.task.TaskDto;
import java.util.Set;

public interface TaskService {
  TaskDto createTask(String workspaceId, String projectId, TaskDto taskDto);

  Set<TaskDto> getTasks(String workspaceId, String projectId);
}
