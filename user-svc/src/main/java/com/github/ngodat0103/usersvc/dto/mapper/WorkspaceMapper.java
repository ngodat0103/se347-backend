package com.github.ngodat0103.usersvc.dto.mapper;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.persistence.document.workspace.Workspace;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceMapper {
  Workspace toDocument(WorkspaceDto workspaceDto);

  WorkspaceDto toDto(Workspace workspace);
}
