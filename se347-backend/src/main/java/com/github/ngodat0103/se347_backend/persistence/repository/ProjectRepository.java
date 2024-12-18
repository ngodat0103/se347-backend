package com.github.ngodat0103.se347_backend.persistence.repository;

import com.github.ngodat0103.se347_backend.persistence.document.project.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project,String> {
    boolean existsByNameAndWorkspaceId(String name, String workspaceId);
}
