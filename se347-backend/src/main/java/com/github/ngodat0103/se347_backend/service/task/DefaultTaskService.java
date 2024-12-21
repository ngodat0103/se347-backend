package com.github.ngodat0103.se347_backend.service.task;

import com.github.ngodat0103.se347_backend.dto.mapper.TaskMapper;
import com.github.ngodat0103.se347_backend.dto.task.TaskDto;
import com.github.ngodat0103.se347_backend.exception.notfound.ProjectNotFoundException;
import com.github.ngodat0103.se347_backend.exception.notfound.UserNotFoundException;
import com.github.ngodat0103.se347_backend.exception.notfound.WorkspaceNotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.task.Task;
import com.github.ngodat0103.se347_backend.persistence.document.task.TaskStatus;
import com.github.ngodat0103.se347_backend.persistence.repository.ProjectRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.TaskRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DefaultTaskService implements TaskService {
  private final TaskRepository taskRepository;
  private final WorkspaceRepository workspaceRepository;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final TaskMapper taskMapper;

  @Override
  public TaskDto createTask(String workspaceId, String projectId, TaskDto taskDto) {

    Task newTask = taskMapper.toDocument(taskDto);
    newTask.setWorkspaceId(workspaceId);
    newTask.setProjectId(projectId);
    newTask.setStatus(TaskStatus.TODO);
    if (!workspaceRepository.existsById(workspaceId)) {
      throw new WorkspaceNotFoundException("id", workspaceId);
    }
    if (!projectRepository.existsById(projectId)) {
      throw new ProjectNotFoundException("id", projectId);
    }
    if (taskDto.getAssigneeId() != null && !userRepository.existsById(taskDto.getAssigneeId())) {
      throw new UserNotFoundException("id", taskDto.getAssigneeId());
    }
    return taskMapper.toDto(taskRepository.save(newTask));
  }

  @Override
  public Set<TaskDto> getTasks(String workspaceId, String projectId) {
    Set<Task> tasks = taskRepository.findByWorkspaceIdAndProjectId(workspaceId, projectId);
    return tasks.stream().map(taskMapper::toDto).collect(Collectors.toUnmodifiableSet());
  }
}
