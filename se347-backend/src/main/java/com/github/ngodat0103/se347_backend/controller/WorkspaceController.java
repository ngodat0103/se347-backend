package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.workspace.AddWorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.workspace.WorkspaceService;
import io.minio.errors.InternalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "Create Workspace", description = "Create a new workspace")
    @ApiResponse(responseCode = "201", description = "Workspace created successfully")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceDto create(@RequestBody @Valid WorkspaceDto workspaceDto) {
        return workspaceService.create(workspaceDto);
    }

    @Operation(summary = "Get Workspaces", description = "Retrieve all workspaces for the current user")
    @ApiResponse(responseCode = "200", description = "Workspaces retrieved successfully")
    @GetMapping(path = "/me")
    public Set<WorkspaceDto> getWorkspaces() {
        return workspaceService.getWorkspaces();
    }

    @Operation(summary = "Add Member to Workspace", description = "Invite a user to join a workspace")
    @ApiResponse(responseCode = "201", description = "Member added successfully")
    @PostMapping(path = "/{workspaceId}/members/addByEmail")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceDto addByEmail(
        @PathVariable String workspaceId,
        @RequestBody @Valid AddWorkspaceMemberDto addWorkspaceMemberDto) {
        return workspaceService.addMember(workspaceId, addWorkspaceMemberDto.getEmail());
    }

    @Operation(summary = "Upload Workspace Image", description = "Upload an image for a workspace")
    @ApiResponse(responseCode = "200", description = "Image uploaded successfully")
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

    @Operation(summary = "Update Workspace", description = "Update workspace details")
    @ApiResponse(responseCode = "200", description = "Workspace updated successfully")
    @PatchMapping("/{workspaceId}")
    public WorkspaceDto update(@PathVariable String workspaceId, @RequestBody WorkspaceDto workspaceDto) {
        return workspaceService.update(workspaceId, workspaceDto);
    }

    @Operation(summary = "Delete Workspace", description = "Delete a workspace")
    @ApiResponse(responseCode = "204", description = "Workspace deleted successfully")
    @DeleteMapping("/{workspaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String delete(@PathVariable String workspaceId) {
        return workspaceService.delete(workspaceId);
    }
}