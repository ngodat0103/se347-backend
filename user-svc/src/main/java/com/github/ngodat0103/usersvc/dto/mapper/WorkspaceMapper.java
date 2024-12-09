package com.github.ngodat0103.usersvc.dto.mapper;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.persistence.document.workspace.Workspace;
import java.util.HashSet;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface WorkspaceMapper {
  @Mapping(target = "members", ignore = true)
  Workspace toDocument(WorkspaceDto workspaceDto);

  default WorkspaceDto toDto(Workspace workspace) {
    WorkspaceDto.WorkspaceDtoBuilder workspaceDto = WorkspaceDto.builder();

    workspaceDto.id(workspace.getId());
    workspaceDto.name(workspace.getName());
    Set<String> list = workspace.getProjects();
    if (list != null) {
      workspaceDto.projects(new HashSet<>(list));
    }
    workspaceDto.imageUrl(workspace.getImageUrl());
    Set<String> members = new HashSet<>();
    var workspaceMembers = workspace.getMembers();
    if (workspaceMembers != null) {
      workspaceMembers.forEach((userId, role) -> members.add(userId));
    }
    workspaceDto.members(members);
    workspaceDto.createdDate(workspace.getCreatedDate());
    workspaceDto.lastUpdatedDate(workspace.getLastUpdatedDate());
    return workspaceDto.build();
  }
}
