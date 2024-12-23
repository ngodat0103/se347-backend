package com.github.ngodat0103.se347_backend.exception.notfound;

public class ProjectNotFoundException extends NotFoundException {
  private static final String NOT_FOUND_TEMPLATE = "Project with %s: %s not found";

  public ProjectNotFoundException(String attribute, String value) {
    super(String.format(NOT_FOUND_TEMPLATE, attribute, value));
  }
}
