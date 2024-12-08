package com.github.ngodat0103.usersvc.service.workspace;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface WorkspaceService {

  Mono<WorkspaceDto> create(WorkspaceDto workspaceDto, String ownerId);

  Mono<String> updatePicture(
      String workspaceId, String accountId, InputStream inputStream, String contentType)
      throws IOException;

  Mono<WorkspaceDto> update(String workspaceId, WorkspaceDto workspaceDto, String accountId);

  Mono<Void> delete(String id);

  Mono<WorkspaceDto> get(String id);

  Mono<Set<WorkspaceDto>> getWorkspaces(String accountId);
}
