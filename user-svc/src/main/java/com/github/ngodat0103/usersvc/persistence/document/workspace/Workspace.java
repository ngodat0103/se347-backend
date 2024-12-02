package com.github.ngodat0103.usersvc.persistence.document.workspace;

import com.github.ngodat0103.usersvc.persistence.document.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "workspaces")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@CompoundIndex(name = "workspace_idx", def = "{'workspaceName': 1,'members': 1}", unique = true)
public class Workspace extends BaseDocument {
  @MongoId private String workspaceId;
  private String workspaceName;
  private WorkspaceProperty workspaceProperty;
  private String workspacePictureUrl;
  private boolean softDeleted;
}
