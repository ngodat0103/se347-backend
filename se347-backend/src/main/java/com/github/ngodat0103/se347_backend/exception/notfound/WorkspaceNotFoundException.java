package com.github.ngodat0103.se347_backend.exception.notfound;

public class WorkspaceNotFoundException extends NotFoundException{
    private static final String NOT_FOUND_TEMPLATE = "Workspace with %s: %s not found";
    public WorkspaceNotFoundException(String attribute, String value) {
        super(String.format(NOT_FOUND_TEMPLATE, attribute, value));
    }
}
