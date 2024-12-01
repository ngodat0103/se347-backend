package com.github.ngodat0103.usersvc.persistence.document;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Set;

@Document(collection = "workspaces")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Workspace extends BaseDocument {
    @MongoId
    private String workspaceId;
    private String workspaceName;
    private Set<String> members;
    private String workspacePictureUrl;

    private boolean softDeleted;
}