package com.github.ngodat0103.se347_backend.service.workspace;

import com.github.ngodat0103.se347_backend.dto.workspace.WorkspaceDto;
import com.github.ngodat0103.se347_backend.service.BaseService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.springframework.http.MediaType;

public interface WorkspaceService extends BaseService<WorkspaceDto> {
  WorkspaceDto addMember(String workspaceId, String email);

  Set<WorkspaceDto> getWorkspaces();

  String uploadImageWorkspace(String workspaceId, InputStream inputStream, MediaType mediaType)
      throws IOException;
}
