package com.github.ngodat0103.usersvc.service.workspace;

import static com.github.ngodat0103.usersvc.exception.Util.createConflictException;

import com.github.ngodat0103.usersvc.config.minio.MinioProperties;
import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.dto.mapper.WorkspaceMapper;
import com.github.ngodat0103.usersvc.dto.mapper.WorkspaceMapperImpl;
import com.github.ngodat0103.usersvc.persistence.document.workspace.Workspace;
import com.github.ngodat0103.usersvc.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.usersvc.service.MinioService;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@Service
@Slf4j
public class DefaultWorkspaceService implements WorkspaceService {
  private static final String WORKSPACE_IDX = "workspace_idx";
  private static final String WORKSPACE_STORAGE_PREFIX = "workspace/";
  private final MinioService minioService;
  private final WorkspaceRepository workspaceRepository;
  private final WorkspaceMapper workspaceMapper = new WorkspaceMapperImpl();
  private final MinioProperties minioProperties;

  @Override
  public Mono<WorkspaceDto> create(WorkspaceDto workspaceDto, String ownerId) {
    var newWorkspace = workspaceMapper.toDocument(workspaceDto);
    newWorkspace.setOwner(ownerId);
    Instant instantNow = Instant.now();
    newWorkspace.setCreatedDate(instantNow);
    newWorkspace.setLastUpdatedDate(instantNow);
    return workspaceRepository
        .save(newWorkspace)
        .doOnSubscribe(data -> log.info("Creating a new workspace"))
        .doOnError(DuplicateKeyException.class, e -> handleDuplicateKey(e, workspaceDto.getName()))
        .doOnSuccess(
            workspace -> log.info("New workspace {} created successfully", workspace.getId()))
        .map(workspaceMapper::toDto);
  }

  @Override
  public Mono<String> updatePicture(
      String workspaceId, String accountId, InputStream inputStream, String contentType) {
    String objectPublicUrl =
        minioProperties.getEndpoint()
            + "/"
            + minioProperties.getBucket()
            + "/"
            + WORKSPACE_STORAGE_PREFIX
            + workspaceId;
    return workspaceRepository
        .findById(workspaceId)
        .flatMap(workspace -> checkPermission(workspace, accountId))
        .doOnNext(workspace -> uploadWorkspaceImageAsync(workspaceId, inputStream, contentType))
        .map(
            workspace -> {
              workspace.setImageUrl(objectPublicUrl);
              return workspace;
            })
        .doOnNext(this::updateWorkspaceDocumentAsync)
        .thenReturn(objectPublicUrl);
  }

  private void uploadWorkspaceImageAsync(
      String workspaceId, InputStream inputStream, String contentType) {
    try {
      log.info("Uploading workspace picture for workspace with id: {}", workspaceId);
      minioService
          .uploadFile(
              WORKSPACE_STORAGE_PREFIX + workspaceId,
              inputStream,
              inputStream.available(),
              contentType)
          .subscribeOn(Schedulers.boundedElastic())
          .subscribe();
    } catch (IOException e) {
      log.error("Error uploading workspace picture for workspace with id: {}", workspaceId);
      log.error(e.getMessage());
    }
  }

  private void updateWorkspaceDocumentAsync(Workspace workspace) {
    workspaceRepository
        .save(workspace)
        .doOnSubscribe(data -> log.info("Updating workspace with id: {}", workspace.getId()))
        .doOnSuccess(
            data -> log.info("Workspace updated successfully with id: {}", workspace.getId()))
        .doOnError(
            throwable -> log.error("Error updating workspace with id: {}", workspace.getId()))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
  }

  private Mono<Workspace> checkPermission(Workspace workspace, String accountId) {
    if (workspace.getOwner().equals(accountId)) {
      return Mono.just(workspace);
    }
    return Mono.error(
        new AccessDeniedException("You do not have permission to edit this workspace"));
  }

  private void handleDuplicateKey(
      DuplicateKeyException duplicateKeyException, String workspaceName) {
    if (duplicateKeyException.getMessage().contains(WORKSPACE_IDX)) {
      throw createConflictException(log, "workspace", "name", workspaceName);
    }
  }

  @Override
  public Mono<WorkspaceDto> update(
      String workspaceId, WorkspaceDto workspaceDto, String accountId) {
    return workspaceRepository
        .findById(workspaceId)
        .flatMap(w -> checkPermission(w, accountId))
        .map(
            w -> {
              w.setName(workspaceDto.getName());
              return w;
            })
        .flatMap(workspaceRepository::save)
        .map(workspaceMapper::toDto);
  }

  @Override
  public Mono<Void> delete(String id) {
    return workspaceRepository
        .deleteById(id)
        .doOnSubscribe(data -> log.info("Deleting workspace with id: {}", id))
        .doOnSuccess(data -> log.info("Workspace deleted successfully with id: {}", id))
        .doOnError(throwable -> log.error("Error deleting workspace with id: {}", id));
  }

  @Override
  public Mono<WorkspaceDto> get(String id) {
    return null;
  }

  @Override
  public Mono<Set<WorkspaceDto>> getWorkspaces(String accountId) {
    return workspaceRepository
        .findByOwner(accountId)
        .doOnSubscribe(data -> log.info("Getting workspaces for account with id: {}", accountId))
        .map(workspaceMapper::toDto)
        .collect(Collectors.toSet())
        .doOnError(
            throwable -> log.error("Error getting workspaces for account with id: {}", accountId))
        .doOnSuccess(
            data ->
                log.info("Workspaces retrieved successfully for account with id: {}", accountId));
  }
}
