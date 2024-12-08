package com.github.ngodat0103.usersvc.persistence.document.workspace;

import com.github.ngodat0103.usersvc.persistence.document.BaseDocument;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "workspaces")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@CompoundIndex(name = "workspace_idx", def = "{'name': 1,'owner': 1}", unique = true)
public class Workspace extends BaseDocument {
  @MongoId private String id;
  private String name;
  private String owner;
  private Map<String, WorkspaceRole> members;
  private Set<String> projects;
  private String imageUrl;
  private boolean softDeleted;
}
