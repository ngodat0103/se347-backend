package com.github.ngodat0103.usersvc.controller;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.service.workspace.WorkspaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
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

  @PostMapping(
      value = "/{workspaceId}/image",
      consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE},
      produces = MediaType.TEXT_PLAIN_VALUE)
  public Mono<String> updatePicture(
      @RequestBody Flux<DataBuffer> dataBufferFlux,
      @PathVariable String workspaceId,
      ServerHttpRequest request,
      Authentication authentication) {
    String contentType = Objects.requireNonNull(request.getHeaders().getContentType()).toString();

    Mono<InputStream> inputStreamMono =
        DataBufferUtils.join(dataBufferFlux).map(DataBuffer::asInputStream);

    return inputStreamMono.flatMap(
        inputStream -> {
          try {
            return workspaceService.updatePicture(
                workspaceId, authentication.getName(), inputStream, contentType);
          } catch (IOException e) {
            log.error("Failed to update image workspace", e);
            return Mono.error(e);
          }
        });
  }

  @GetMapping(path = "/me")
  public Mono<Set<WorkspaceDto>> getWorkspaces(Authentication authentication) {
    return workspaceService.getWorkspaces(authentication.getName());
  }

  @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return workspaceService.delete(id);
    }
}
