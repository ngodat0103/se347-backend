package com.github.ngodat0103.usersvc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WorkspaceDto {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String id;

  @NotNull(message = "Name is required")
  private String name;


  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Set<String> members;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Set<String> projects;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String imageUrl;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant createdDate;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant lastUpdatedDate;
}
