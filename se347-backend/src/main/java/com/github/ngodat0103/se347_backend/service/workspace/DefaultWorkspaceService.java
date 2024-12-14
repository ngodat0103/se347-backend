package com.github.ngodat0103.se347_backend.service.workspace;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.dto.mapper.WorkspaceMapper;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.exception.ConflictException;
import com.github.ngodat0103.se347_backend.exception.NotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.account.Account;
import com.github.ngodat0103.se347_backend.persistence.document.account.AccountStatus;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.*;
import com.github.ngodat0103.se347_backend.persistence.repository.AccountRepository;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DefaultWorkspaceService implements WorkspaceService {
  private final WorkspaceRepository workspaceRepository;
  private final AccountRepository accountRepository;
  private final WorkspaceMapper workspaceMapper;

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
    Account invitedAccount =
        accountRepository
            .findByEmailAndAccountStatus(email, AccountStatus.ACTIVE)
            .orElseThrow(() -> new NotFoundException("User with this email is not exists"));
    Workspace callerWorkspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new NotFoundException("Workspace with this id is not found"));
    checkPermission(callerWorkspace, callerUserId);

    Map<String, WorkSpaceMember> memberMap = callerWorkspace.getMembers();
    memberMap.put(
        invitedAccount.getAccountId(),
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
  public Map<String, WorkspaceMemberDto> getWorkspaceMember(String workspaceId) {

    Workspace callerWorkspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new NotFoundException("Workspace with this Id is not found"));

    LinkedHashMap<String, WorkspaceMemberDto> workspaceMemberDtoLinkedHashMap =
        new LinkedHashMap<>();

    callerWorkspace
        .getMembers()
        .forEach(
            (k, v) -> {
              Account currentAccount =
                  accountRepository
                      .findById(k)
                      .orElseThrow(
                          () -> new NotFoundException("Account with this Id is not found"));
              WorkspaceMemberDto workspaceMemberDto1 =
                  WorkspaceMemberDto.builder()
                      .nickName(currentAccount.getNickName())
                      .email(currentAccount.getEmail())
                      .role(v.getRole())
                      .status(v.getStatus())
                      .build();
              workspaceMemberDtoLinkedHashMap.put(k, workspaceMemberDto1);
            });
    return workspaceMemberDtoLinkedHashMap;
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
