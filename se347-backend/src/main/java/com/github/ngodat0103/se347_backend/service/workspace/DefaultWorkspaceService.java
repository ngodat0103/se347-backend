package com.github.ngodat0103.se347_backend.service.workspace;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.config.minio.MinioProperties;
import com.github.ngodat0103.se347_backend.dto.mapper.WorkspaceMapper;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.NotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import com.github.ngodat0103.se347_backend.persistence.document.user.UserStatus;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.*;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.ngodat0103.se347_backend.service.minio.DefaultMinioService;
import com.github.ngodat0103.se347_backend.service.minio.MinioService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DefaultWorkspaceService implements WorkspaceService {
  private final WorkspaceRepository workspaceRepository;
  private final UserRepository userRepository;
  private final WorkspaceMapper workspaceMapper;
  private final MinioProperties minioProperties;
  private static final String WORKSPACE_STORAGE_PREFIX = "workspace/";
  private final MinioService minioService;

  @Override
  public WorkspaceDto create(WorkspaceDto workspaceDto) {
    String accountId = getUserIdFromAuthentication();

    Workspace workspace = new Workspace(workspaceDto.getName());
    if (workspaceRepository.existsByNameAndOwnerId(workspace.getName(), accountId)) {
      throw new ConflictException(
          "Workspace name already exists", ConflictException.Type.ALREADY_EXISTS);
    }
    workspace.setOwnerId(accountId);
    Instant instantNow = Instant.now();
    workspace.setCreatedDate(instantNow);
    workspace.setLastUpdatedDate(instantNow);
    workspace = workspaceRepository.save(workspace);
    return workspaceMapper.toDto(workspace);
  }

  @Override
  public WorkspaceDto update(WorkspaceDto workspaceDto) {
    return null;
  }

  @Override
  public WorkspaceDto delete(Long id) {
    return null;
  }

  @Override
  public WorkspaceDto addMember(String workspaceId, String email) {
    String callerUserId = getUserIdFromAuthentication();
    User invitedUser =
        userRepository
            .findByEmailAndUserStatus(email, UserStatus.ACTIVE)
            .orElseThrow(() -> new NotFoundException("User with this email is not exists"));
    Workspace callerWorkspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new NotFoundException("Workspace with this id is not found"));
    checkPermission(callerWorkspace, callerUserId);

    Map<String, WorkSpaceMember> memberMap = callerWorkspace.getMembers();
    memberMap.put(
        invitedUser.getAccountId(),
        new WorkSpaceMember(WorkspaceRole.MEMBER, WorkSpaceMemberStatus.PENDING));
    callerWorkspace = workspaceRepository.save(callerWorkspace);
    return workspaceMapper.toDto(callerWorkspace);
  }

  @Override
  public Set<WorkspaceDto> getWorkspaces() {
    String accountId = getUserIdFromAuthentication();
    Set<Workspace> workspaces = workspaceRepository.findByOwnerIdOrMemberId(accountId);

    return workspaces.stream().map(workspaceMapper::toDto).collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public String uploadImageWorkspace(String workspaceId, InputStream inputStream, MediaType mediaType) throws IOException {
    Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new NotFoundException("Workspace with this Id is not found"));
    String callerUserId = getUserIdFromAuthentication();
    checkPermission(workspace,callerUserId);
    String imagePublicUrl =  minioService.uploadFile(WORKSPACE_STORAGE_PREFIX+workspaceId,inputStream, inputStream.available(),mediaType);
    workspace.setImageUrl(imagePublicUrl);
    workspaceRepository.save(workspace);
    return imagePublicUrl;
  }

  private void checkPermission(Workspace workspace, String accountId) {
    if (workspace.getOwnerId().equals(accountId)) {
      return;
    }
    WorkSpaceMember workSpaceMember = workspace.getMembers().get(accountId);
    if (workSpaceMember == null) {
      throw new AccessDeniedException("You do not have permission to edit this resource");
    }
  }
}
