package com.github.ngodat0103.usersvc.persistence.repository;

import com.github.ngodat0103.usersvc.persistence.document.workspace.Workspace;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface WorkspaceRepository extends ReactiveMongoRepository<Workspace, String> {
  Flux<Workspace> findByOwner(String ownerId);
  //    Flux<Workspace> findByMembersUserId(String userId);
}
