package com.github.ngodat0103.se347_backend.service.project;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.dto.mapper.ProjectMapper;
import com.github.ngodat0103.se347_backend.dto.mapper.ProjectMapperImpl;
import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.NotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.project.Project;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.Workspace;
import com.github.ngodat0103.se347_backend.persistence.repository.ProjectRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.minio.MinioService;
import com.github.ngodat0103.se347_backend.service.workspace.WorkspaceService;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DefaultProjectService implements ProjectService {
  private final ProjectRepository projectRepository;
  private final WorkspaceRepository workspaceRepository;
  private final WorkspaceService workspaceService;
  private final ProjectMapper projectMapper = new ProjectMapperImpl();
  private final MinioService minioService;

  @Override
  public ProjectDto get(String workspaceId, String projectId) {
    return null;
  }

  @Override
  public Set<ProjectDto> getProjects(String workspaceId) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(
                () -> new NotFoundException("Workspace with id " + workspaceId + " not found"));
    String callUserId = getUserIdFromAuthentication();
    workspaceService.checkReadPermission(workspace, callUserId);
    Set<String> projectIds = workspace.getProjects();
    if (projectIds == null) {
      return Set.of();
    }
    List<Project> projects = projectRepository.findAllById(projectIds);

    return projects.stream().map(projectMapper::toDto).collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public ProjectDto create(String workspaceId, ProjectDto projectDto) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new NotFoundException("Workspace not found"));
    Project project = projectMapper.toDocument(projectDto);
    if (projectRepository.existsByNameAndWorkspaceId(project.getName(), workspaceId)) {
      throw new ConflictException(
          "Project name already exists for this workspace", ConflictException.Type.ALREADY_EXISTS);
    }
    String callUserId = getUserIdFromAuthentication();
    workspaceService.checkWritePermission(workspace, callUserId);
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
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new NotFoundException("Workspace not found"));
    this.workspaceService.checkWritePermission(workspace, getUserIdFromAuthentication());
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));
    String objectName = "workspace/" + workspaceId + "/project/" + projectId + "/image";
    String publicUrl;
    try {
      publicUrl = minioService.uploadFile(objectName, image, image.available(), mediaType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    project.setImageUrl(publicUrl);
    projectRepository.save(project);
    return publicUrl;
  }
}
