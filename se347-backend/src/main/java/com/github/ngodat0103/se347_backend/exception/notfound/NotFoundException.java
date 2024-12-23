package com.github.ngodat0103.se347_backend.exception.notfound;

public abstract class NotFoundException extends RuntimeException {

  protected NotFoundException(String message) {
    super(message);
  }
}
