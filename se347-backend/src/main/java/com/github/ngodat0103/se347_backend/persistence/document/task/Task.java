package com.github.ngodat0103.se347_backend.persistence.document.task;

import java.time.Instant;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "tasks")
public class Task {
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
