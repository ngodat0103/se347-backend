package com.github.ngodat0103.se347_backend.persistence.repository;

import com.github.ngodat0103.se347_backend.persistence.document.task.Task;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TaskRepository extends MongoRepository<Task, String> {

  Set<Task> findByWorkspaceIdAndProjectId(String workspaceId, String projectId);

  Optional<Task> findByIdAndProjectId(String id, String projectId);

  Optional<Task> findByIdAndProjectIdAndWorkspaceId(
      String id, String projectId, String workspaceId);

  @Query(value = "{ 'workspaceId' : ?0, 'projectId' : ?1 }", sort = "{ 'position' : -1 }")
  Optional<Task> findMaxPositionByWorkspaceIdAndProjectId(String workspaceId, String projectId);
}
