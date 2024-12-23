package com.github.ngodat0103.se347_backend.persistence.repository;

import com.github.ngodat0103.se347_backend.persistence.document.task.Task;
import java.util.Set;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRepository extends MongoRepository<Task, String> {

  Set<Task> findByWorkspaceIdAndProjectId(String workspaceId, String projectId);
}
