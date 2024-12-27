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
import com.github.ngodat0103.se347_backend.persistence.repository.ProjectRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.TaskRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.authtz.AuthZService;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
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
  private final AuthZService authZService;

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
    newTask.setPosition(generatePosition(workspaceId, projectId));
    Task savedTask = taskRepository.save(newTask);
    return getTaskDto(savedTask);
  }

  @Override
  public ResponseTaskDto updateTask(
      String workspaceId, String projectId, String taskId, UpdateTaskDto updateTaskDto) {
    this.authZService.checkWriteTasksPermission(workspaceId);
    Task callerTask =
        taskRepository
            .findByIdAndProjectIdAndWorkspaceId(taskId, projectId, workspaceId)
            .orElseThrow(() -> new TaskNotFoundException("id", taskId));
    callerTask.setStatus(updateTaskDto.getStatus());
    callerTask.setAssigneeId(updateTaskDto.getAssigneeId());
    callerTask.setName(updateTaskDto.getName());
    callerTask.setDescription(updateTaskDto.getDescription());
    callerTask.setPosition(updateTaskDto.getPosition());
    Project callerProject =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException("id", projectId));
    callerTask.setProjectId(projectId);
    Task savedTask = taskRepository.save(callerTask);
    log.info("Task with id {} has been updated", taskId);
    ResponseTaskDto responseTaskDto = taskMapper.toDto(savedTask);
    responseTaskDto.setProject(projectMapper.toDto(callerProject));
    if (savedTask.getAssigneeId() != null) {
      User assignee = userRepository.findById(savedTask.getAssigneeId()).orElse(null);
      responseTaskDto.setAssignee(userMapper.toDto(assignee));
    }
    return responseTaskDto;
  }

  @Override
  public void deleteTask(String workspaceId, String projectId, String taskId) {
    this.authZService.checkWriteTasksPermission(workspaceId);
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
        .sorted(Comparator.comparing(ResponseTaskDto::getPosition))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public ResponseTaskDto getTaskById(String workspaceId, String projectId, String taskId) {

    this.authZService.checkReadTasksPermission(workspaceId);
    var callerTask =
        taskRepository
            .findByIdAndProjectIdAndWorkspaceId(taskId, projectId, workspaceId)
            .orElseThrow(() -> new TaskNotFoundException("id", taskId));
    return getTaskDto(callerTask);
  }

  private int generatePosition(String workspaceId, String projectId) {
    List<Task> tasks =
        taskRepository
            .findMaxPositionByWorkspaceIdAndProjectId(workspaceId, projectId);

    int currentMaxPosition = tasks.getFirst().getPosition();
    if (currentMaxPosition == 0) {
      return 1000;
    }
    return currentMaxPosition + 1000;
  }
}
