package com.github.ngodat0103.usersvc.persistence.document.workspace;

import java.util.Map;
import lombok.Data;

@Data
public class WorkspaceProperty {
  private String ownerId;
  private Map<String, WorkSpacePermission> members;
}
