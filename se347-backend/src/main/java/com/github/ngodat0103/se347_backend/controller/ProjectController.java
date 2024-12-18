package com.github.ngodat0103.se347_backend.controller;


import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import com.github.ngodat0103.se347_backend.service.project.ProjectService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(path = "/api/v1/workspaces/{workspaceId}/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto createProject(@PathVariable String workspaceId, @RequestBody @Valid ProjectDto projectDto) {
        return  projectService.create(workspaceId, projectDto);
    }

    @GetMapping
    public Set<ProjectDto> getProject(@PathVariable String workspaceId) {
        return projectService.getProjects(workspaceId);
    }
}
