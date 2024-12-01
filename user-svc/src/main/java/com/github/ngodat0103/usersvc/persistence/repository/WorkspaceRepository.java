package com.github.ngodat0103.usersvc.persistence.repository;

import com.github.ngodat0103.usersvc.persistence.document.Workspace;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface WorkspaceRepository extends ReactiveMongoRepository<Workspace, String> {}
