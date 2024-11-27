package com.github.ngodat0103.usersvc.exception;

public class InvalidEmailCodeException extends RuntimeException {
  public InvalidEmailCodeException() {
    super("Invalid email code");
  }
}
