package com.github.ngodat0103.se347_backend.persistence.document.task;

import com.github.ngodat0103.se347_backend.persistence.document.BaseDocument;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "tasks")
public class Task extends BaseDocument {
  @MongoId private String id;
  private String name;
  private TaskStatus status;
  private String assigneeId;
  private String projectId;
  private int position;
  private String workspaceId;
  private Instant dueDate;
  private String description;
}
