package com.github.ngodat0103.se347_backend.service.project;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;
import static com.github.ngodat0103.se347_backend.service.ServiceUtil.*;

import com.github.ngodat0103.se347_backend.dto.mapper.ProjectMapper;
import com.github.ngodat0103.se347_backend.dto.mapper.ProjectMapperImpl;
import com.github.ngodat0103.se347_backend.dto.project.ProjectAnalyticsDto;
import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import com.github.ngodat0103.se347_backend.dto.task.DateRange;
import com.github.ngodat0103.se347_backend.dto.task.TaskAnalytics;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.notfound.ProjectNotFoundException;
import com.github.ngodat0103.se347_backend.exception.notfound.WorkspaceNotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.project.Project;
import com.github.ngodat0103.se347_backend.persistence.document.task.Task;
import com.github.ngodat0103.se347_backend.persistence.document.task.TaskStatus;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.Workspace;
import com.github.ngodat0103.se347_backend.persistence.repository.ProjectRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.TaskRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.authtz.AuthZService;
import com.github.ngodat0103.se347_backend.service.minio.MinioService;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultProjectService implements ProjectService {
  private final ProjectRepository projectRepository;
  private final WorkspaceRepository workspaceRepository;
  private final TaskRepository taskRepository;
  private final AuthZService authZService;
  private final ProjectMapper projectMapper;
  private final MinioService minioService;

  @Override
  public ProjectDto getProjectById(String workspaceId, String projectId) {
    String callUserId = getUserIdFromAuthentication();
    this.authZService.checkReadWorkspacePermission(workspaceId);
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException("id", projectId));

    log.info("User {} get project {}", callUserId, projectId);
    return projectMapper.toDto(project);
  }

  @Override
  public ProjectDto updateProject(String workspaceId, String projectId, ProjectDto projectDto) {
    String callUserId = getUserIdFromAuthentication();
    this.authZService.checkWriteWorkspacePermission(workspaceId);
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException("id", projectId));
    project.setLastUpdatedDate(Instant.now());
    project.setName(projectDto.getName());
    log.info("User {} update project {}", callUserId, projectId);
    return projectMapper.toDto(projectRepository.save(project));
  }

  @Override
  public String deleteProject(String workspaceId, String projectId) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new WorkspaceNotFoundException("id", workspaceId));
    String callerUserId = getUserIdFromAuthentication();
    this.authZService.checkWriteWorkspacePermission(workspaceId);
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException("id", projectId));
    projectRepository.delete(project);
    Set<String> projectIds = workspace.getProjects();
    if (projectIds != null) {
      projectIds.remove(projectId);
      workspace.setProjects(projectIds);
      workspace.setLastUpdatedDate(Instant.now());
      Set<Task> tasksAssociatedWithProject = taskRepository.findByWorkspaceIdAndProjectId(workspaceId, projectId);
      taskRepository.deleteAll(tasksAssociatedWithProject);
      workspaceRepository.save(workspace);
    }
    log.info("User {} delete project {}", callerUserId, projectId);
    return "Project deleted successfully";
  }

  @Override
  public Set<ProjectDto> getProjects(String workspaceId) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new WorkspaceNotFoundException("id", workspaceId));
    this.authZService.checkReadWorkspacePermission(workspaceId);
    Set<String> projectIds = workspace.getProjects();
    if (projectIds == null) {
      return Set.of();
    }
    List<Project> projects = projectRepository.findAllById(projectIds);

    return projects.stream().map(projectMapper::toDto)
            .sorted(Comparator.comparing(ProjectDto::getName))
            .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public ProjectDto create(String workspaceId, ProjectDto projectDto) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new WorkspaceNotFoundException("id", workspaceId));
    Project project = projectMapper.toDocument(projectDto);
    if (projectRepository.existsByNameAndWorkspaceId(project.getName(), workspaceId)) {
      throw new ConflictException(
          "Project name already exists for this workspace", ConflictException.Type.ALREADY_EXISTS);
    }
    this.authZService.checkWriteWorkspacePermission(workspaceId);
    project.setWorkspaceId(workspaceId);
    Instant now = Instant.now();
    project.setCreatedDate(now);
    project.setLastUpdatedDate(now);
    project = projectRepository.save(project);
    Set<String> projectIds = workspace.getProjects();
    if (projectIds == null) {
      projectIds = new LinkedHashSet<>();
    }
    projectIds.add(project.getId());
    workspace.setProjects(projectIds);
    workspace.setLastUpdatedDate(now);
    workspaceRepository.save(workspace);
    return projectMapper.toDto(project);
  }

  @Override
  public String updateImageProject(
      String workspaceId, String projectId, InputStream image, MediaType mediaType) {
    this.authZService.checkWriteWorkspacePermission(workspaceId);
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException("id", projectId));
    String objectName = "workspace/" + workspaceId + "/project/" + projectId + "/image";
    String publicUrl;
    try {
      publicUrl = minioService.uploadFile(objectName, image, image.available(), mediaType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    project.setImageUrl(publicUrl);
    project.setLastUpdatedDate(Instant.now());
    projectRepository.save(project);
    String callerUserId = getUserIdFromAuthentication();
    log.info("User {} update image project {}", callerUserId, projectId);
    return publicUrl;
  }

  @Override
  public ProjectAnalyticsDto getProjectAnalytics(String workspaceId, String projectId) {
    this.authZService.checkReadWorkspacePermission(workspaceId);

    DateRange currentMonthRange = getDateRangeForCurrentMonth();
    DateRange lastMonthRange = getDateRangeForLastMonth();

    List<Task> currentMonthTasks =
        taskRepository.findTaskByWorkspaceIdAndProjectIdAndCreatedDateBetween(
            workspaceId, projectId, currentMonthRange.getStart(), currentMonthRange.getEnd());
    List<Task> lastMonthTasks =
        taskRepository.findTaskByWorkspaceIdAndProjectIdAndCreatedDateBetween(
            workspaceId, projectId, lastMonthRange.getStart(), lastMonthRange.getEnd());

    TaskAnalytics currentMonthAnalytics = computeTaskAnalytics(currentMonthTasks);
    TaskAnalytics lastMonthAnalytics = computeTaskAnalytics(lastMonthTasks);

    return ProjectAnalyticsDto.builder()
        .taskCount(currentMonthAnalytics.getTaskCount())
        .taskDifference(currentMonthAnalytics.getTaskCount() - lastMonthAnalytics.getTaskCount())
        .assignedTaskCount(currentMonthAnalytics.getAssignedTaskCount())
        .assignedTaskDifference(
            currentMonthAnalytics.getAssignedTaskCount()
                - lastMonthAnalytics.getAssignedTaskCount())
        .completedTaskCount(currentMonthAnalytics.getCompletedTaskCount())
        .completedTaskDifference(
            currentMonthAnalytics.getCompletedTaskCount()
                - lastMonthAnalytics.getCompletedTaskCount())
        .inCompletedTaskCount(currentMonthAnalytics.getInCompletedTaskCount())
        .inCompletedTaskDifference(
            (currentMonthAnalytics.getTaskCount() - currentMonthAnalytics.getCompletedTaskCount())
                - (lastMonthAnalytics.getTaskCount() - lastMonthAnalytics.getCompletedTaskCount()))
        .overdueTaskCount(currentMonthAnalytics.getOverdueTaskCount())
        .overdueTaskDifference(
            currentMonthAnalytics.getOverdueTaskCount() - lastMonthAnalytics.getOverdueTaskCount())
        .build();
  }

  private TaskAnalytics computeTaskAnalytics(List<Task> tasks) {
    int taskCount = tasks.size();
    int assignedTaskCount =
        (int) tasks.stream().filter(task -> task.getAssigneeId() != null).count();
    int completedTaskCount =
        (int) tasks.stream().filter(task -> task.getStatus().equals(TaskStatus.DONE)).count();
    int inCompletedTaskCount = taskCount - completedTaskCount;
    int overdueTaskCount =
        (int)
            tasks.stream()
                .filter(
                    task -> task.getDueDate() != null && task.getDueDate().isBefore(Instant.now()))
                .count();
    return new TaskAnalytics(
        taskCount, assignedTaskCount, completedTaskCount, inCompletedTaskCount, overdueTaskCount);
  }
}
