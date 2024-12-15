package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.workspace.AddWorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.workspace.WorkspaceService;
import io.minio.errors.InternalException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/workspaces")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class WorkspaceController {
  private WorkspaceService workspaceService;
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WorkspaceDto create(@RequestBody @Valid WorkspaceDto workspaceDto) {
    return workspaceService.create(workspaceDto);
  }
  @GetMapping(path = "/me")
  public Set<WorkspaceDto> getWorkspaces() {
    return workspaceService.getWorkspaces();
  }

  @PostMapping(path = "/{workspaceId}/members/addByEmail")
  @ResponseStatus(HttpStatus.CREATED)
  public WorkspaceDto addByEmail(
      @PathVariable String workspaceId,
      @RequestBody @Valid AddWorkspaceMemberDto addWorkspaceMemberDto) {
    return workspaceService.addMember(workspaceId, addWorkspaceMemberDto.getEmail());
  }

  @PostMapping(
      value = "/{workspaceId}/image",
      consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE},
      produces = MediaType.TEXT_PLAIN_VALUE)
  public String updatePicture(
      @RequestBody byte[] imageBytes, @PathVariable String workspaceId, HttpServletRequest request)
      throws Exception {

    try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
      return workspaceService.uploadImageWorkspace(
          workspaceId, inputStream, MediaType.valueOf(request.getContentType()));

    } catch (IOException e) {
      log.error("Failed to update image workspace", e);
      throw new InternalException("Error when upload image {}", e.toString());
    }
  }


      @DeleteMapping("/{workspaceId}")
      @ResponseStatus(HttpStatus.NO_CONTENT)
      public String delete(@PathVariable String workspaceId) {
          return workspaceService.delete(workspaceId);
      }
}
