package com.github.ngodat0103.se347_backend.service.authtz;

import static com.github.ngodat0103.se347_backend.security.SecurityUtil.*;

import com.github.ngodat0103.se347_backend.exception.notfound.WorkspaceNotFoundException;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.WorkSpaceMember;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.WorkspaceRole;
import com.github.ngodat0103.se347_backend.persistence.repository.WorkspaceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultAuthZService implements AuthZService {

  private WorkspaceRepository workspaceRepository;

  @Override
  public void checkReadWorkspacePermission(String workspaceId) {
    var callerRole = this.getWorkspaceRole(workspaceId);
    if (callerRole == null) {
      log.info(
          "User {} doesn't have permission to read workspace {}",
          getUserIdFromAuthentication(),
          workspaceId);
      throw new AccessDeniedException("You don't have permission to read this workspace");
    }
  }

  @Override
  public void checkWriteWorkspacePermission(String workspaceId) {
    var callerRole = this.getWorkspaceRole(workspaceId);
    if (callerRole == null || callerRole.ordinal() >= WorkspaceRole.DEVELOPER.ordinal()) {
      throw new AccessDeniedException(
          "You don't have permission to write this workspace, your Role is " + callerRole);
    }
  }

  @Override
  public void checkReadTasksPermission(String workspaceId) {
    var callerRole = this.getWorkspaceRole(workspaceId);
    if (callerRole == null) {
      throw new AccessDeniedException(
          "You don't have permission to read this task, your Role is " + callerRole);
    }
  }

  @Override
  public void checkWriteTasksPermission(String workspaceId) {
    var callerRole = this.getWorkspaceRole(workspaceId);
    if (callerRole == null || callerRole.ordinal() >= WorkspaceRole.MEMBER.ordinal()) {
      throw new AccessDeniedException(
          "You don't have permission to write this task, your Role is " + callerRole);
    }
  }

  private WorkspaceRole getWorkspaceRole(String workspaceId) {
    var callerWorkspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(() -> new WorkspaceNotFoundException("id", workspaceId));
    var callerUserId = getUserIdFromAuthentication();
    WorkSpaceMember caller = callerWorkspace.getMembers().getOrDefault(callerUserId, null);
    return caller != null ? caller.getRole() : null;
  }
}
