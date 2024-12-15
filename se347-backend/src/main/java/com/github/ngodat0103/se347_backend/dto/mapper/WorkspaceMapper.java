package com.github.ngodat0103.se347_backend.dto.mapper;

import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.Workspace;

public interface WorkspaceMapper {
  WorkspaceDto toDto(Workspace workspace);
}
