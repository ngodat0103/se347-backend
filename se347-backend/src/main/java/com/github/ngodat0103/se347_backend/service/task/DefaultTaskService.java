package com.github.ngodat0103.se347_backend.service.task;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.dto.mapper.ProjectMapper;
import com.github.ngodat0103.se347_backend.dto.mapper.TaskMapper;
import com.github.ngodat0103.se347_backend.dto.mapper.UserMapper;
import com.github.ngodat0103.se347_backend.dto.task.CreateTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.ResponseTaskDto;
import com.github.ngodat0103.se347_backend.dto.task.UpdateTaskDto;
import com.github.ngodat0103.se347_backend.exception.notfound.ProjectNotFoundException;
import com.github.ngodat0103.se347_backend.exception.notfound.TaskNotFoundException;
import com.github.ngodat0103.se347_backend.exception.notfound.UserNotFoundException;
import com.github.ngodat0103.se347_backend.exception.notfound.WorkspaceNotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.project.Project;
import com.github.ngodat0103.se347_backend.persistence.document.task.Task;
import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.Workspace;
import com.github.ngodat0103.se347_backend.persistence.repository.ProjectRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.TaskRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.workspace.WorkspaceService;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultTaskService implements TaskService {
  private final TaskRepository taskRepository;
  private final WorkspaceRepository workspaceRepository;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final TaskMapper taskMapper;
  private final UserMapper userMapper;
  private final ProjectMapper projectMapper;
  private final WorkspaceService workspaceService;

  @Override
  public ResponseTaskDto createTask(
      String workspaceId, String projectId, CreateTaskDto createTaskDto) {

    Task newTask = taskMapper.toDocument(createTaskDto);
    newTask.setWorkspaceId(workspaceId);
    newTask.setProjectId(projectId);
    if (!workspaceRepository.existsById(workspaceId)) {
      throw new WorkspaceNotFoundException("id", workspaceId);
    }
    if (!projectRepository.existsById(projectId)) {
      throw new ProjectNotFoundException("id", projectId);
    }
    if (createTaskDto.getAssigneeId() != null
        && !userRepository.existsById(createTaskDto.getAssigneeId())) {
      throw new UserNotFoundException("id", createTaskDto.getAssigneeId());
    }
    Task savedTask = taskRepository.save(newTask);
    return getTaskDto(savedTask);
  }

  @Override
  public ResponseTaskDto updateTask(
      String workspaceId, String projectId, String taskId, UpdateTaskDto updateTaskDto) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new WorkspaceNotFoundException("id", workspaceId));
    this.workspaceService.checkWritePermission(workspace, getUserIdFromAuthentication());
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException("id", projectId));
    Task task =
        taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("id", taskId));
    task.setStatus(updateTaskDto.getStatus());
    task.setAssigneeId(updateTaskDto.getAssigneeId());
    task.setName(updateTaskDto.getName());
    task.setDescription(updateTaskDto.getDescription());
    task.setProjectId(projectId);
    Task savedTask = taskRepository.save(task);
    log.info("Task with id {} has been updated", taskId);
    ResponseTaskDto responseTaskDto = taskMapper.toDto(savedTask);
    responseTaskDto.setProject(projectMapper.toDto(project));
    if (savedTask.getAssigneeId() != null) {
      User assignee = userRepository.findById(savedTask.getAssigneeId()).orElse(null);
      responseTaskDto.setAssignee(userMapper.toDto(assignee));
    }
    return responseTaskDto;
  }

  @Override
  public void deleteTask(String workspaceId, String projectId, String taskId) {

    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new WorkspaceNotFoundException("id", workspaceId));
    this.workspaceService.checkWritePermission(workspace, getUserIdFromAuthentication());
    if (!projectRepository.existsById(projectId)) {
      throw new ProjectNotFoundException("id", projectId);
    }
    if (!taskRepository.existsById(taskId)) {
      throw new TaskNotFoundException("id", taskId);
    }
    taskRepository.deleteById(taskId);
    log.info("Task with id {} has been deleted", taskId);
  }

  @NotNull
  private ResponseTaskDto getTaskDto(Task savedTask) {
    ResponseTaskDto savedCreateTaskDto = taskMapper.toDto(savedTask);
    if (savedTask.getAssigneeId() != null) {
      User assignee = userRepository.findById(savedTask.getAssigneeId()).orElse(null);
      savedCreateTaskDto.setAssignee(userMapper.toDto(assignee));
    }
    Project project = projectRepository.findById(savedTask.getProjectId()).orElse(null);
    savedCreateTaskDto.setProject(projectMapper.toDto(project));
    return savedCreateTaskDto;
  }

  @Override
  public Set<ResponseTaskDto> getTasks(String workspaceId, String projectId) {
    Set<Task> tasks = taskRepository.findByWorkspaceIdAndProjectId(workspaceId, projectId);
    return tasks.stream()
        .map(this::getTaskDto)
        .sorted(Comparator.comparing(ResponseTaskDto::getStatus))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public ResponseTaskDto getTaskById(String workspaceId, String projectId, String taskId) {
    Workspace callerWorkspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new WorkspaceNotFoundException("id", workspaceId));
    this.workspaceService.checkReadPermission(callerWorkspace, getUserIdFromAuthentication());
    Task task =
        taskRepository
            .findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new TaskNotFoundException("id", taskId));
    return getTaskDto(task);
  }
}
