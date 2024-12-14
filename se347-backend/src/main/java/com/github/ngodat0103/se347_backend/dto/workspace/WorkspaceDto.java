package com.github.ngodat0103.se347_backend.dto.workspace;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.LinkedHashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class WorkspaceDto {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String workspaceId;

  @NotNull(message = "Name is required")
  private String name;

  private String ownerId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Setter
  private LinkedHashMap<String, WorkspaceMemberDto> members;

  //  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  //  private Set<String> projects;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String imageUrl;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant createdDate;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant lastUpdatedDate;
}