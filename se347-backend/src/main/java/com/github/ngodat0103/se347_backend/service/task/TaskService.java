package com.github.ngodat0103.se347_backend.service.task;

import com.github.ngodat0103.se347_backend.dto.task.CreateTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.ResponseTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.UpdateTaskDto;
import java.util.Set;

public interface TaskService {
  ResponseTaskDto createTask(String workspaceId, String projectId, CreateTaskDto createTaskDto);

  ResponseTaskDto updateTask(
      String workspaceId, String projectId, String taskId, UpdateTaskDto updateTaskDto);

  void deleteTask(String workspaceId, String projectId, String taskId);

  Set<ResponseTaskDto> getTasks(String workspaceId, String projectId);

  Set<ResponseTaskDto> getTasks(String workspaceId);

  Set<ResponseTaskDto> getMyTasks();

  ResponseTaskDto getTaskById(String workspaceId, String projectId, String taskId);
}
