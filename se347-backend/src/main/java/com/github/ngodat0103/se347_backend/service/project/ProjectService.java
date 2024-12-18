package com.github.ngodat0103.se347_backend.service.project;

import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import java.util.Set;

public interface ProjectService {
    ProjectDto get(String workspaceId, String projectId);
    Set<ProjectDto> getProjects(String workspaceId);
    ProjectDto create(String workspaceId, ProjectDto projectDto);
}
