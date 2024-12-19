package com.github.ngodat0103.se347_backend.service.project;

import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import java.io.InputStream;
import java.util.Set;
import org.springframework.http.MediaType;

public interface ProjectService {
  ProjectDto get(String workspaceId, String projectId);

  Set<ProjectDto> getProjects(String workspaceId);

  ProjectDto create(String workspaceId, ProjectDto projectDto);

  String updateImageProject(
      String workspaceId, String projectId, InputStream image, MediaType mediaType);
}
