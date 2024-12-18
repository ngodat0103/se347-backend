package com.github.ngodat0103.se347_backend.persistence.document.project;

import com.github.ngodat0103.se347_backend.persistence.document.BaseDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "projects")
@Data
@EqualsAndHashCode(callSuper = true)
@CompoundIndex(name = "project_idx", def = "{'workspaceId': 1, 'name': 1}", unique = true)
public class Project  extends BaseDocument {
    @MongoId
    private String id;
    private String workspaceId;
    private String name;
    private String imageUrl;
}
