package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.task.CreateTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.ResponseTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.UpdateTaskDto;
import com.github.ngodat0103.se347_backend.service.task.TaskService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
public class TaskController {
  private final TaskService taskService;
  private static final String TASKS_BASE_PATH =
      "/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks";

  @PostMapping(path = TASKS_BASE_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseTaskDto createTask(
      @PathVariable String workspaceId,
      @PathVariable String projectId,
      @RequestBody @Valid CreateTaskDto createTaskDto) {
    return taskService.createTask(workspaceId, projectId, createTaskDto);
  }

  @GetMapping(path = TASKS_BASE_PATH)
  public Set<ResponseTaskDto> getTasks(
      @PathVariable String workspaceId, @PathVariable String projectId) {
    return taskService.getTasks(workspaceId, projectId);
  }

  @PutMapping(path = TASKS_BASE_PATH + "/{taskId}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseTaskDto updateTask(
      @PathVariable String workspaceId,
      @PathVariable String projectId,
      @PathVariable String taskId,
      @RequestBody @Valid UpdateTaskDto updateTaskDto) {
    return taskService.updateTask(workspaceId, projectId, taskId, updateTaskDto);
  }

  @DeleteMapping(path = TASKS_BASE_PATH + "/{taskId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTask(
      @PathVariable String workspaceId,
      @PathVariable String projectId,
      @PathVariable String taskId) {
    taskService.deleteTask(workspaceId, projectId, taskId);
  }

  @GetMapping(path = TASKS_BASE_PATH + "/{taskId}")
  public ResponseTaskDto getTaskById(
      @PathVariable String workspaceId,
      @PathVariable String projectId,
      @PathVariable String taskId) {
    return taskService.getTaskById(workspaceId, projectId, taskId);
  }

  @GetMapping(path = "/api/v1/workspaces/{workspaceId}/my-tasks")
  public Set<ResponseTaskDto> getMyTasks(@PathVariable String workspaceId) {
    return taskService.getMyTasks(workspaceId);
  }

  @GetMapping(path = "/api/v1/workspaces/{workspaceId}/tasks")
  public Set<ResponseTaskDto> getTasks(@PathVariable String workspaceId) {
    return taskService.getTasks(workspaceId);
  }
}
