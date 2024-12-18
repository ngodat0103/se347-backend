package com.github.ngodat0103.se347_backend.service.workspace;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.dto.mapper.WorkspaceMapper;
import com.github.ngodat0103.se347_backend.dto.workspace.MemberRoleUpdateDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.NotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import com.github.ngodat0103.se347_backend.persistence.document.user.UserStatus;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.*;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.se347_backend.service.minio.MinioService;
import com.nimbusds.jose.util.Base64URL;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
  private static final String WORKSPACE_STORAGE_PREFIX = "workspace/";
  private final MinioService minioService;
  private static final URI BASE_WORKSPACE_FRONTEND_ENDPOINT =
      URI.create("http://localhost:4200/workspaces/join");

  @Override
  public String delete(String workspaceId) {
    Workspace workspace = getWorkspaceById(workspaceId);
    String callerUserId = getUserIdFromAuthentication();
    if (!workspace.getOwnerId().equals(callerUserId)) {
      throw new AccessDeniedException("You do not have permission to delete this resource");
    }
    workspaceRepository.deleteById(workspaceId);
    return "Delete successfully";
  }

  @Override
  public WorkspaceDto create(WorkspaceDto workspaceDto) {
    String callerUserId = getUserIdFromAuthentication();

    Workspace workspace = new Workspace(workspaceDto.getName());
    if (workspaceRepository.existsByNameAndOwnerId(workspace.getName(), callerUserId)) {
      throw new ConflictException(
          "Workspace name already exists", ConflictException.Type.ALREADY_EXISTS);
    }
    workspace.setOwnerId(callerUserId);
    Instant instantNow = Instant.now();
    workspace.setCreatedDate(instantNow);
    workspace.setLastUpdatedDate(instantNow);
    workspace = workspaceRepository.save(workspace);
    workspace.setProjects(new LinkedHashSet<>());
    this.updateInviteCode(workspace);
    workspace = workspaceRepository.save(workspace);
    return workspaceMapper.toDto(workspace);
  }

  @Override
  public WorkspaceDto addMemberByEmail(String workspaceId, String email) {
    String callerUserId = getUserIdFromAuthentication();
    User invitedUser =
        userRepository
            .findByEmailAndUserStatus(email, UserStatus.ACTIVE)
            .orElseThrow(() -> new NotFoundException("User with this email is not exists"));
    if (invitedUser.getUserId().equals(callerUserId)) {
      throw new ConflictException(
          "You can not invite yourself", ConflictException.Type.ALREADY_EXISTS);
    }
    Workspace callerWorkspace = getWorkspaceById(workspaceId);
    checkWritePermission(callerWorkspace, callerUserId);

    Map<String, WorkSpaceMember> memberMap = callerWorkspace.getMembers();
    memberMap.put(
        invitedUser.getUserId(),
        new WorkSpaceMember(WorkspaceRole.MEMBER, WorkSpaceMemberStatus.PENDING));
    callerWorkspace.setLastUpdatedDate(Instant.now());
    callerWorkspace = workspaceRepository.save(callerWorkspace);
    return workspaceMapper.toDto(callerWorkspace);
  }

  @Override
  public WorkspaceDto addMemberByInviteCode(String inviteCode) {
    String callerUserId = getUserIdFromAuthentication();
    Workspace workspace =
        workspaceRepository
            .findByInviteCode(inviteCode)
            .orElseThrow(
                () -> new NotFoundException("Workspace with this invite code is not found"));
    validateUserIsNotMember(workspace, callerUserId);
    addNewMemberToWorkspace(workspace, callerUserId);
    workspace.setLastUpdatedDate(Instant.now());
    workspace = workspaceRepository.save(workspace);
    return workspaceMapper.toDto(workspace);
  }

  @Override
  public WorkspaceDto reGenerateInviteCode(String workspaceId) {
    Workspace callerWorkspace = getWorkspaceById(workspaceId);
    String callerUserId = getUserIdFromAuthentication();
    checkWritePermission(callerWorkspace, callerUserId);
    updateInviteCode(callerWorkspace);
    callerWorkspace = workspaceRepository.save(callerWorkspace);
    return workspaceMapper.toDto(callerWorkspace);
  }

  private void updateInviteCode(Workspace callerWorkspace) {
    String inviteCode = generateInviteCode();
    callerWorkspace.setInviteCode(inviteCode);
    callerWorkspace.setLastUpdatedDate(Instant.now());
  }

  private static String generateInviteCode() {
    byte[] randomBytes = new byte[6];
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(randomBytes);
    return Base64URL.encode(randomBytes).toString();
  }

  @Override
  public WorkspaceDto updateMemberRole(
      String workspaceId, String userId, MemberRoleUpdateDto memberRoleUpdateDto) {
    Workspace callerWorkspace = getWorkspaceById(workspaceId);
    String callerUserId = getUserIdFromAuthentication();
    checkWritePermission(callerWorkspace, callerUserId);
    WorkSpaceMember workSpaceMember = callerWorkspace.getMembers().get(userId);
    if (workSpaceMember == null) {
      throw new NotFoundException("Member with this id is not found");
    }
    workSpaceMember.setRole(memberRoleUpdateDto.getNewRole());
    callerWorkspace.setLastUpdatedDate(Instant.now());

    callerWorkspace = workspaceRepository.save(callerWorkspace);
    return workspaceMapper.toDto(callerWorkspace);
  }

  @Override
  public String removeMember(String workspaceId, String userId) {
    var callerWorkspace = getWorkspaceById(workspaceId);
    var callerUserId = getUserIdFromAuthentication();
    checkWritePermission(callerWorkspace, callerUserId);
    if (callerUserId.equals(userId)) {
      throw new ConflictException(
          "You can not remove yourself", ConflictException.Type.ALREADY_EXISTS);
    }
    callerWorkspace.getMembers().remove(userId);
    callerWorkspace.setLastUpdatedDate(Instant.now());
    workspaceRepository.save(callerWorkspace);
    return "Remove member successfully";
  }

  @Override
  public WorkspaceDto update(String workspaceId, WorkspaceDto workspaceDto) {
    Workspace workspace = getWorkspaceById(workspaceId);
    String callerUserId = getUserIdFromAuthentication();
    checkWritePermission(workspace, callerUserId);
    workspace.setName(workspaceDto.getName());
    workspace.setLastUpdatedDate(Instant.now());
    workspace = workspaceRepository.save(workspace);
    return workspaceMapper.toDto(workspace);
  }

  @Override
  public Set<WorkspaceDto> getWorkspaces() {
    String accountId = getUserIdFromAuthentication();
    Set<Workspace> workspaces = workspaceRepository.findByOwnerIdOrMemberId(accountId);

    return workspaces.stream().map(workspaceMapper::toDto).collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public WorkspaceDto getWorkspaceByInviteCode(String inviteCode) {
    Workspace workspace =
        workspaceRepository
            .findByInviteCode(inviteCode)
            .orElseThrow(
                () -> new NotFoundException("Workspace with this invite code is not found"));
    return workspaceMapper.toDto(workspace);
  }

  @Override
  public String uploadImageWorkspace(
      String workspaceId, InputStream inputStream, MediaType mediaType) throws IOException {
    Workspace workspace = getWorkspaceById(workspaceId);
    String callerUserId = getUserIdFromAuthentication();
    checkWritePermission(workspace, callerUserId);
    String imagePublicUrl =
        minioService.uploadFile(
            WORKSPACE_STORAGE_PREFIX + workspaceId,
            inputStream,
            inputStream.available(),
            mediaType);
    workspace.setImageUrl(imagePublicUrl);
    workspaceRepository.save(workspace);
    return imagePublicUrl;
  }

  private Workspace getWorkspaceById(String workspaceId) {
    return workspaceRepository
        .findById(workspaceId)
        .orElseThrow(() -> new NotFoundException("Workspace with this id is not found"));
  }

  private void validateUserIsNotMember(Workspace workspace, String userId) {
    if (userId.equals(workspace.getOwnerId())) {
      throw new ConflictException(
          "You are the owner of this workspace", ConflictException.Type.ALREADY_EXISTS);
    }
    if (workspace.getMembers().containsKey(userId)) {
      throw new ConflictException(
          "You are already a member of this workspace", ConflictException.Type.ALREADY_EXISTS);
    }
  }

  private void addNewMemberToWorkspace(Workspace workspace, String userId) {
    workspace
        .getMembers()
        .put(userId, new WorkSpaceMember(WorkspaceRole.MEMBER, WorkSpaceMemberStatus.ACTIVE));
  }
}
