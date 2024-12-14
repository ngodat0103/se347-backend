package com.github.ngodat0103.se347_backend.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
  public enum Type {
    ALREADY_EXISTS,
    ALREADY_VERIFIED,
  }

  private final Type type;

  public ConflictException(String message, Type type) {
    super(message);
    this.type = type;
  }
}
