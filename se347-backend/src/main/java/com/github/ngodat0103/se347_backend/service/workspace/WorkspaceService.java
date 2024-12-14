package com.github.ngodat0103.se347_backend.service.workspace;

import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceMemberDto;
import com.github.ngodat0103.se347_backend.service.BaseService;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public interface WorkspaceService extends BaseService<WorkspaceDto> {
  WorkspaceDto addMember(String workspaceId, String email);

  Set<WorkspaceDto> getWorkspaces();

  String uploadImageWorkspace(String workspaceId, InputStream inputStream, MediaType mediaType) throws IOException;

}
