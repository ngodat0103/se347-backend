package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.workspace.AddWorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.workspace.WorkspaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/workspaces")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class WorkspaceController {
  private final WorkspaceRepository workspaceRepository;
  private WorkspaceService workspaceService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WorkspaceDto create(@RequestBody @Valid WorkspaceDto workspaceDto) {
    return workspaceService.create(workspaceDto);
  }

  //    @PostMapping(
  //            value = "/{workspaceId}/image",
  //            consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE},
  //            produces = MediaType.TEXT_PLAIN_VALUE)
  //    public Mono<String> updatePicture(
  //            @RequestBody Flux<DataBuffer> dataBufferFlux,
  //            @PathVariable String workspaceId,
  //            ServerHttpRequest request,
  //            Authentication authentication) {
  //        String contentType =
  // Objects.requireNonNull(request.getHeaders().getContentType()).toString();
  //
  //        Mono<InputStream> inputStreamMono =
  //                DataBufferUtils.join(dataBufferFlux).map(DataBuffer::asInputStream);
  //
  //        return inputStreamMono.flatMap(
  //                inputStream -> {
  //                    try {
  //                        return workspaceService.updatePicture(
  //                                workspaceId, authentication.getName(), inputStream,
  // contentType);
  //                    } catch (IOException e) {
  //                        log.error("Failed to update image workspace", e);
  //                        return Mono.error(e);
  //                    }
  //                });
  //    }
  //
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
  //
  //    @DeleteMapping("/{id}")
  //    public Mono<Void> delete(@PathVariable String id) {
  //        return workspaceService.delete(id);
  //    }
}
