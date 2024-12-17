package com.github.ngodat0103.se347_backend.persistence.repository;

import com.github.ngodat0103.se347_backend.persistence.document.workspace.Workspace;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface WorkspaceRepository extends MongoRepository<Workspace, String> {
  boolean existsByNameAndOwnerId(String name, String ownerId);

  @Query("{$or: [{'ownerId': ?0},{'members.?0':{$exists:  true}}]}")
  LinkedHashSet<Workspace> findByOwnerIdOrMemberId(String accountId);

  Optional<Workspace> findByInviteCode_InviteCode(String inviteCodeUrl);
}
