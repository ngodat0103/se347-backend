package com.github.ngodat0103.se347_backend.exception.notfound;

public class UserNotFoundException extends NotFoundException {
  private static final String NOT_FOUND_TEMPLATE = "User with %s: %s: not found";

  public UserNotFoundException(String attribute, String value) {
    super(String.format(NOT_FOUND_TEMPLATE, attribute, value));
  }
}
