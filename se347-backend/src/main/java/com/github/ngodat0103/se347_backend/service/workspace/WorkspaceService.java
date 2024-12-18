package com.github.ngodat0103.se347_backend.service.workspace;

import com.github.ngodat0103.se347_backend.dto.workspace.MemberRoleUpdateDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.service.BaseService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.springframework.http.MediaType;

public interface WorkspaceService extends BaseService<WorkspaceDto> {
  WorkspaceDto create(WorkspaceDto workspaceDto);

  WorkspaceDto addMemberByEmail(String workspaceId, String email);

  WorkspaceDto addMemberByInviteCode(String inviteCode);

  WorkspaceDto reGenerateInviteCode(String workspaceId);

  WorkspaceDto updateMemberRole(
      String workspaceId, String memberId, MemberRoleUpdateDto memberRoleUpdateDto);

  String removeMember(String workspaceId, String userId);

  Set<WorkspaceDto> getWorkspaces();

  WorkspaceDto getWorkspaceByInviteCode(String inviteCode);

  String uploadImageWorkspace(String workspaceId, InputStream inputStream, MediaType mediaType)
      throws IOException;
}
