package com.github.ngodat0103.se347_backend.dto.workspace;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.WorkSpaceMemberStatus;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.WorkspaceRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkspaceMemberDto {
  private String email;
  private String nickName;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private WorkspaceRole role;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private WorkSpaceMemberStatus status;
}
