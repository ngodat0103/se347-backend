package com.github.ngodat0103.usersvc.controller;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.service.WorkspaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/workspaces")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class WorkspaceController {
  private final WorkspaceService workspaceService;

  @PostMapping
  public Mono<WorkspaceDto> create(
      @RequestBody @Valid WorkspaceDto workspaceDto, Authentication authentication) {
    String accountId = authentication.getName();
    return workspaceService.create(workspaceDto, accountId);
  }

  @PostMapping("/{workspaceId}/picture")
  public Mono<WorkspaceDto> updatePicture(
      @RequestParam String workspaceId, @RequestPart("image") FilePart filePart) {
    return null;
  }

  public Mono<Void> delete(String id) {
    return null;
  }

  public Mono<WorkspaceDto> get(WorkspaceDto workspaceDto) {
    return null;
  }
}
