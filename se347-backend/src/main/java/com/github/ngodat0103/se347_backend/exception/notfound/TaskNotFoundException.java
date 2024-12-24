package com.github.ngodat0103.se347_backend.exception.notfound;

public class TaskNotFoundException extends NotFoundException {

  private static final String NOT_FOUND_TEMPLATE = "Task with %s: %s not found";

  public TaskNotFoundException(String attribute, String value) {
    super(String.format(NOT_FOUND_TEMPLATE, attribute, value));
  }
}
