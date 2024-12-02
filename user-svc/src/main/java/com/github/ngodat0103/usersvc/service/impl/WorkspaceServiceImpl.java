package com.github.ngodat0103.usersvc.service.impl;

import static com.github.ngodat0103.usersvc.exception.Util.createConflictException;

import com.github.ngodat0103.usersvc.config.minio.MinioProperties;
import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.dto.mapper.WorkspaceMapper;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.document.workspace.WorkSpacePermission;
import com.github.ngodat0103.usersvc.persistence.document.workspace.Workspace;
import com.github.ngodat0103.usersvc.persistence.document.workspace.WorkspaceProperty;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.usersvc.service.MinioService;
import com.github.ngodat0103.usersvc.service.WorkspaceService;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
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
public class WorkspaceServiceImpl implements WorkspaceService {
  private final MinioService minioService;
  private WorkspaceRepository workspaceRepository;
  private WorkspaceMapper workspaceMapper;
  private UserRepository userRepository;
  private static final String WORKSPACE_IDX = "workspace_idx";
  private static final String WORKSPACE_STORAGE_PREFIX = "workspace/";
  private final MinioProperties minioProperties;

  @Override
  public Mono<WorkspaceDto> create(WorkspaceDto workspaceDto, String accountId) {
    var newWorkspace = workspaceMapper.toDocument(workspaceDto);
    WorkspaceProperty workspaceProperty = new WorkspaceProperty();
    workspaceProperty.setOwnerId(accountId);
    newWorkspace.setWorkspaceProperty(workspaceProperty);
    return workspaceRepository
        .save(newWorkspace)
        .doOnSubscribe(data -> log.info("Creating a new workspace"))
        .doOnSuccess(
            workspace ->
                updateAccountWorkspace(accountId, workspace)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe())
        .doOnError(
            DuplicateKeyException.class,
            e -> handleDuplicateKey(e, workspaceDto.getWorkspaceName()))
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
        .filter(workspace -> checkPermission(workspace, accountId))
        .switchIfEmpty(
            Mono.error(
                new AccessDeniedException("You do not have permission to edit this workspace")))
        .doOnNext(
            workspace -> this.uploadWorkspacePictureAsync(workspaceId, inputStream, contentType))
        .map(
            workspace -> {
              workspace.setWorkspacePictureUrl(objectPublicUrl);
              return workspace;
            })
        .doOnNext(this::updateWorkspaceDocumentAsync)
        .thenReturn(objectPublicUrl);
  }

  private void uploadWorkspacePictureAsync(
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
        .doOnSubscribe(
            data -> log.info("Updating workspace with id: {}", workspace.getWorkspaceId()))
        .doOnSuccess(
            data ->
                log.info("Workspace updated successfully with id: {}", workspace.getWorkspaceId()))
        .doOnError(
            throwable ->
                log.error("Error updating workspace with id: {}", workspace.getWorkspaceId()))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
  }

  private boolean checkPermission(Workspace workspace, String accountId) {
    var workspaceProperty = workspace.getWorkspaceProperty();
    if (workspaceProperty.getOwnerId().equals(accountId)) {
      return true;
    }
    WorkSpacePermission workSpacePermission =
        workspaceProperty.getMembers().getOrDefault(accountId, null);
    return workSpacePermission != null && workSpacePermission.isCanEdit();
  }

  private void handleDuplicateKey(
      DuplicateKeyException duplicateKeyException, String workspaceName) {
    if (duplicateKeyException.getMessage().contains(WORKSPACE_IDX)) {
      throw createConflictException(log, "workspace", "workspaceName", workspaceName);
    }
  }

  private Mono<Account> updateAccountWorkspace(String accountId, Workspace workspace) {
    return userRepository
        .findById(accountId)
        .doOnSubscribe(data -> log.info("Updating the account.workspaces with id: {}", accountId))
        .map(
            account -> {
              Set<String> workspaces = account.getWorkspaces();
              if (workspaces == null) {
                workspaces = new HashSet<>();
              }
              workspaces.add(workspace.getWorkspaceId());
              account.setWorkspaces(workspaces);
              return account;
            })
        .flatMap(userRepository::save)
        .doOnError(
            throwable -> log.error("Error updating account.workspaces with id: {}", accountId))
        .doOnSuccess(
            data -> log.info("Account.workspaces updated successfully with id: {}", accountId));
  }

  @Override
  public Mono<WorkspaceDto> update(WorkspaceDto workspaceDto) {
    return null;
  }

  @Override
  public Mono<Void> delete(String id) {
    return null;
  }

  @Override
  public Mono<WorkspaceDto> get(String id) {
    return null;
  }

  @Override
  public Mono<Set<WorkspaceDto>> getWorkspaces(String accountId) {
    return userRepository
        .findById(accountId)
        .doOnSubscribe(data -> log.info("Getting workspaces for account with id: {}", accountId))
        .map(Account::getWorkspaces)
        .flatMapMany(workspaceRepository::findAllById)
        .map(workspaceMapper::toDto)
        .collect(Collectors.toSet())
        .doOnError(
            throwable -> log.error("Error getting workspaces for account with id: {}", accountId))
        .doOnSuccess(
            data ->
                log.info("Workspaces retrieved successfully for account with id: {}", accountId));
  }
}
