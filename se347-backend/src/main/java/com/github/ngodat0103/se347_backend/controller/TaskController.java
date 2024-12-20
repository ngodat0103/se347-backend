package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.task.TaskDto;
import com.github.ngodat0103.se347_backend.service.task.TaskService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
public class TaskController {
  private final TaskService taskService;

  @PostMapping
  public TaskDto createTask(
      @PathVariable String workspaceId,
      @PathVariable String projectId,
      @RequestBody @Valid TaskDto taskDto) {
    return taskService.createTask(workspaceId, projectId, taskDto);
  }

  @GetMapping
  public Set<TaskDto> getTasks(@PathVariable String workspaceId, @PathVariable String projectId) {
    return taskService.getTasks(workspaceId, projectId);
  }
}
