package com.github.ngodat0103.se347_backend.persistence.document.workspace;

import com.github.ngodat0103.se347_backend.persistence.document.BaseDocument;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "workspaces")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@CompoundIndex(name = "workspace_idx", def = "{'name': 1, 'ownerId':1}", unique = true)
public class Workspace extends BaseDocument {
  @MongoId private String workspaceId;
  private String name;
  private String ownerId;
  private Map<String, WorkSpaceMember> members;
  private Set<String> projects;
  private String imageUrl;
  private boolean softDeleted;

  public Workspace(String name) {
    this.name = name;
    this.members = new LinkedHashMap<>();
  }
}
