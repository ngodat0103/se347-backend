package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.project.ProjectDto;
import com.github.ngodat0103.se347_backend.service.project.ProjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/workspaces/{workspaceId}/projects")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
public class ProjectController {
  private final ProjectService projectService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProjectDto createProject(
      @PathVariable String workspaceId, @RequestBody @Valid ProjectDto projectDto) {
    return projectService.create(workspaceId, projectDto);
  }

  @GetMapping
  public Set<ProjectDto> getProject(@PathVariable String workspaceId) {
    return projectService.getProjects(workspaceId);
  }

  @PostMapping(
      value = "/{projectId}/image",
      consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE},
      produces = MediaType.TEXT_PLAIN_VALUE)
  public String updateImage(
      @RequestBody byte[] imageBytes,
      @PathVariable String workspaceId,
      @PathVariable String projectId,
      HttpServletRequest request) {
    InputStream inputStream = new ByteArrayInputStream(imageBytes);
    return projectService.updateImageProject(
        workspaceId, projectId, inputStream, MediaType.valueOf(request.getContentType()));
  }
}
