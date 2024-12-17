package com.github.ngodat0103.se347_backend.persistence.document.workspace;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;

@Data
@AllArgsConstructor
public class WorkspaceInviteCode {
    private String inviteCode;
     private URI inviteCodeUri;
}
