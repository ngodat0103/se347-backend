package com.github.ngodat0103.se347_backend.dto.mapper;

import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.persistence.document.account.Account;
import com.github.ngodat0103.se347_backend.persistence.document.workspace.Workspace;
import com.github.ngodat0103.se347_backend.persistence.repository.AccountRepository;
import java.util.LinkedHashMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DefaultWorkspaceMapper implements WorkspaceMapper {

  private final AccountRepository accountRepository;

  @Override
  public WorkspaceDto toDto(Workspace workspace) {

    LinkedHashMap<String, WorkspaceMemberDto> workspaceMemberDtoLinkedHashMap =
        new LinkedHashMap<>();

    workspace
        .getMembers()
        .forEach(
            (k, v) -> {
              Account currentAccount = accountRepository.findById(k).orElse(null);
              if (currentAccount != null) {
                WorkspaceMemberDto workspaceMemberDto =
                    WorkspaceMemberDto.builder()
                        .nickName(currentAccount.getNickName())
                        .email(currentAccount.getEmail())
                        .status(v.getStatus())
                        .role(v.getRole())
                        .build();
                workspaceMemberDtoLinkedHashMap.put(k, workspaceMemberDto);
              }
            });

    return WorkspaceDto.builder()
        .id(workspace.getId())
        .name(workspace.getName())
            .ownerId(workspace.getOwnerId())
        .createdDate(workspace.getCreatedDate())
        .lastUpdatedDate(workspace.getLastUpdatedDate())
        .members(workspaceMemberDtoLinkedHashMap)
        .build();
  }
}
