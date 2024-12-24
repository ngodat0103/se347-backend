package com.github.ngodat0103.se347_backend.service.authtz;

import org.springframework.security.access.AccessDeniedException;

public interface AuthZService {
  void checkReadWorkspacePermission(String workspaceId) throws AccessDeniedException;

  void checkWriteWorkspacePermission(String workspaceId) throws AccessDeniedException;

  void checkReadTasksPermission(String workspaceId) throws AccessDeniedException;

  void checkWriteTasksPermission(String workspaceId) throws AccessDeniedException;
}
