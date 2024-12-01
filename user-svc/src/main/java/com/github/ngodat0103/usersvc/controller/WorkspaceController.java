package com.github.ngodat0103.usersvc.controller;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.service.MinioService;
import com.github.ngodat0103.usersvc.service.WorkspaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
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
  private final MinioService minioService;

  @PostMapping
  public Mono<WorkspaceDto> create(
      @RequestBody @Valid WorkspaceDto workspaceDto, Authentication authentication) {
    String accountId = authentication.getName();
    return workspaceService.create(workspaceDto, accountId);
  }

  @PostMapping(
      value = "/{workspaceId}/picture",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> updatePicture(
      @PathVariable String workspaceId,
      @RequestPart("image") FilePart filePart,
      Authentication authentication) {

    return filePart
        .content()
        .collectList()
        .map(List::getFirst)
        .map(DataBuffer::asInputStream)
        .flatMap(
            inputstream -> {
              try {
                return workspaceService.updatePicture(
                    workspaceId,
                    authentication.getName(),
                    inputstream,
                    Objects.requireNonNull(filePart.headers().getContentType()).toString());
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });

    //   throw new UnsupportedOperationException();
  }

  public Mono<Void> delete(String id) {
    return null;
  }

  public Mono<WorkspaceDto> get(WorkspaceDto workspaceDto) {
    return null;
  }

  @GetMapping(path = "/me")
  public Mono<Set<WorkspaceDto>> getWorkspaces(Authentication authentication) {
    return workspaceService.getWorkspaces(authentication.getName());
  }
}
