package com.github.ngodat0103.usersvc.dto.mapper;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.persistence.document.workspace.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface WorkspaceMapper {
  @Mapping(target = "members", ignore = true)
  Workspace toDocument(WorkspaceDto workspaceDto);



  default WorkspaceDto toDto(Workspace workspace){
    WorkspaceDto.WorkspaceDtoBuilder workspaceDto = WorkspaceDto.builder();

    workspaceDto.id( workspace.getId() );
    workspaceDto.name( workspace.getName() );
    workspaceDto.description( workspace.getDescription() );
    Set<String> list = workspace.getProjects();
    if ( list != null ) {
      workspaceDto.projects( new HashSet<>( list ) );
    }
    workspaceDto.imageUrl( workspace.getImageUrl() );
    Set<String> members = new HashSet<>();
    workspace.getMembers().forEach((userId,role) -> members.add(userId));
    workspaceDto.members(members);
    return workspaceDto.build();
  }
}
