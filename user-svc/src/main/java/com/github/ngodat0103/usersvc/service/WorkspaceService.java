package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface WorkspaceService {

    Mono<WorkspaceDto> create(WorkspaceDto workspaceDto, String accountId);
    Mono<WorkspaceDto> update(WorkspaceDto workspaceDto);
    Mono<Void> delete(String id);
    Mono<WorkspaceDto> get(String id);
    Mono<Set<WorkspaceDto>> getWorkspaces(String accountId);
}
