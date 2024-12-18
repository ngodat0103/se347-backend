package com.github.ngodat0103.se347_backend.service.project;

import com.github.ngodat0103.se347_backend.dto.mapper.ProjectMapper;
import com.github.ngodat0103.se347_backend.dto.mapper.ProjectMapperImpl;
import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.NotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.project.Project;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.Workspace;
import com.github.ngodat0103.se347_backend.persistence.repository.ProjectRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.workspace.WorkspaceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class DefaultProjectService implements ProjectService{
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceService workspaceService;
    private final ProjectMapper projectMapper = new ProjectMapperImpl();

    @Override
    public ProjectDto get(String workspaceId, String projectId) {
       return null;
    }

    @Override
    public Set<ProjectDto> getProjects(String workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace with id " + workspaceId + " not found"));
        String callUserId = getUserIdFromAuthentication();
        workspaceService.checkReadPermission(workspace,callUserId);
        Set<String> projectIds = workspace.getProjects();
        if(projectIds.isEmpty()) {
            return Set.of();
        }
        List<Project> projects = projectRepository.findAllById(projectIds);


        return projects.stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public ProjectDto create(String workspaceId, ProjectDto projectDto) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        Project project = projectMapper.toDocument(projectDto);
        if(projectRepository.existsByNameAndWorkspaceId(project.getName(),workspaceId)){
            throw new ConflictException("Project name already exists for this workspace", ConflictException.Type.ALREADY_EXISTS);
        }
        String callUserId = getUserIdFromAuthentication();
        workspaceService.checkWritePermission(workspace,callUserId);
        project.setWorkspaceId(workspaceId);
        Instant now = Instant.now();
        project.setCreatedDate(now);
        project.setLastUpdatedDate(now);
        project = projectRepository.save(project);
        workspace.getProjects().add(project.getId());
        workspace.setLastUpdatedDate(now);
        workspaceRepository.save(workspace);
        return projectMapper.toDto(project);
    }
}
