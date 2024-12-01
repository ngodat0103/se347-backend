package com.github.ngodat0103.usersvc.controller;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.service.WorkspaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/workspaces")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class WorkspaceController  {
    private final WorkspaceService workspaceService;

   @PostMapping
    public Mono<WorkspaceDto> create(WorkspaceDto workspaceDto, Authentication authentication) {
        return null;
    }

    public Mono<WorkspaceDto> update(WorkspaceDto workspaceDto) {
        return null;
    }

    public Mono<Void> delete(String id) {
        return null;
    }

    public Mono<WorkspaceDto> get(WorkspaceDto workspaceDto) {
        return null;
    }
}
