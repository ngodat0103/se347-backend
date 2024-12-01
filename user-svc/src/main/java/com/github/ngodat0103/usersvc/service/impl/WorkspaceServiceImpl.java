package com.github.ngodat0103.usersvc.service.impl;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.dto.mapper.WorkspaceMapper;
import com.github.ngodat0103.usersvc.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.usersvc.service.WorkspaceService;
import lombok.AllArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;

@AllArgsConstructor
@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    private WorkspaceRepository workspaceRepository;
    private WorkspaceMapper workspaceMapper;

    @Override
    public Mono<WorkspaceDto> create(WorkspaceDto workspaceDto, String accountId) {
        return null;
    }

    @Override
    public Mono<WorkspaceDto> update(WorkspaceDto workspaceDto) {
        return null;
    }

    @Override
    public Mono<Void> delete(String id) {
        return null;
    }

    @Override
    public Mono<WorkspaceDto> get(String id) {
        return null;
    }

    @Override
    public Mono<Set<WorkspaceDto>> getWorkspaces(String accountId) {
        return null;
    }
}
