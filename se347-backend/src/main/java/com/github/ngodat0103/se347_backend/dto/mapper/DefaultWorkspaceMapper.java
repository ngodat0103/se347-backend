package com.github.ngodat0103.se347_backend.dto.mapper;

import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.Workspace;
import com.github.ngodat0103.se347_backend.persistence.repository.UserRepository;
import java.util.LinkedHashMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DefaultWorkspaceMapper implements WorkspaceMapper {

  private final UserRepository userRepository;

  @Override
  public WorkspaceDto toDto(Workspace workspace) {

    LinkedHashMap<String, WorkspaceMemberDto> workspaceMemberDtoLinkedHashMap =
        new LinkedHashMap<>();

    workspace
        .getMembers()
        .forEach(
            (k, v) -> {
              User currentUser = userRepository.findById(k).orElse(null);
              if (currentUser != null) {
                WorkspaceMemberDto workspaceMemberDto =
                    WorkspaceMemberDto.builder()
                        .nickName(currentUser.getNickName())
                        .email(currentUser.getEmail())
                        .status(v.getStatus())
                        .role(v.getRole())
                        .build();
                workspaceMemberDtoLinkedHashMap.put(k, workspaceMemberDto);
              }
            });

    return WorkspaceDto.builder()
        .workspaceId(workspace.getWorkspaceId())
        .name(workspace.getName())
        .ownerId(workspace.getOwnerId())
        .imageUrl(workspace.getImageUrl())
        .createdDate(workspace.getCreatedDate())
        .lastUpdatedDate(workspace.getLastUpdatedDate())
        .members(workspaceMemberDtoLinkedHashMap)
        .build();
  }
}
