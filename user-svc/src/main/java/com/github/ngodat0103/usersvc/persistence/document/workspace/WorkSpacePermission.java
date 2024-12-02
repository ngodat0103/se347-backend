package com.github.ngodat0103.usersvc.persistence.document.workspace;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkSpacePermission {
  private boolean canEdit;
  private boolean canDelete;
  private boolean canInvite;
  private boolean canView;
}
