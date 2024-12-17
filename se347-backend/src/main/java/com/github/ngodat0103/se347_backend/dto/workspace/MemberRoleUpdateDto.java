package com.github.ngodat0103.se347_backend.dto.workspace;


import com.github.ngodat0103.se347_backend.persistence.document.workspace.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberRoleUpdateDto {
    @NotNull(message = "newRole is required")
    private WorkspaceRole newRole;
}
