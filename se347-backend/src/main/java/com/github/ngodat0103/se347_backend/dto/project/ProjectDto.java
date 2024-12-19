package com.github.ngodat0103.se347_backend.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectDto {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String id;

  @NotNull(message = "Name is required")
  private String name;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String imageUrl;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String workspaceId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant createdDate;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant lastUpdatedDate;
}
