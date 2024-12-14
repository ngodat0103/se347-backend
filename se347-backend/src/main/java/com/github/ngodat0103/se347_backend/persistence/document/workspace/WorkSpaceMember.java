package com.github.ngodat0103.se347_backend.persistence.document.workspace;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkSpaceMember {
  private WorkspaceRole role;
  private WorkSpaceMemberStatus status;
}
