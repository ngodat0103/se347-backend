package com.github.ngodat0103.se347_backend.persistence.document.workspace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.net.URI;
import java.net.URL;

@Data
@AllArgsConstructor
public class WorkspaceInviteCode {
    private String inviteCode;
     private URI inviteCodeUri;
}
