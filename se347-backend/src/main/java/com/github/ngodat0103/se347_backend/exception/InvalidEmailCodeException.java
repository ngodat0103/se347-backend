package com.github.ngodat0103.se347_backend.exception;

public class InvalidEmailCodeException extends RuntimeException {
  public InvalidEmailCodeException() {
    super("Invalid email code");
  }
}
