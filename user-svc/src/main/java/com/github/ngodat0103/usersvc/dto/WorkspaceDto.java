package com.github.ngodat0103.usersvc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WorkspaceDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String workspaceId;
    @NotNull(message = "Workspace name is required")
    private String workspaceName;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String workspacePictureUrl;
}
