package com.github.ngodat0103.se347_backend.persistence.repository;

import com.github.ngodat0103.se347_backend.persistence.document.task.Task;
import java.time.Instant;
import java.util.List;
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
  List<Task> findMaxPositionByWorkspaceIdAndProjectId(String workspaceId, String projectId);

  List<Task> findTaskByWorkspaceIdAndProjectIdAndCreatedDateBetween(
      String workspaceId, String projectId, Instant startDayOfMonth, Instant endDayOfMonth);

  List<Task> findTaskByWorkspaceIdAndCreatedDateBetween(
      String workspaceId, Instant startDayOfMonth, Instant endDayOfMonth);

  List<Task> findByAssigneeId(String assigneeId);

  List<Task> findByWorkspaceId(String workspaceId);

  List<Task> findByWorkspaceIdAndAssigneeId(String workspaceId, String assigneeId);
}
