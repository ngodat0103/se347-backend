package com.github.ngodat0103.se347_backend.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @Getter
  static class Error {
    private final String detail;
    private final String pointer;
    @JsonIgnore private static final String JSON_PATH_SCHEMA = "#/";

    public Error(String detail, String pointer) {
      this.detail = detail;
      this.pointer = JSON_PATH_SCHEMA + pointer;
    }
  }

  @ExceptionHandler({ConflictException.class})
  @ResponseStatus(HttpStatus.CONFLICT)
  public ProblemDetail handleApiException(ConflictException e, HttpServletRequest request) {
    ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problemDetails.setDetail(e.getMessage());
    problemDetails.setInstance(URI.create(request.getContextPath()));
    problemDetails.setTitle(e.getType().toString());
    problemDetails.setType(URI.create("https://problems-registry.smartbear.com/already-exists"));
    return problemDetails;
  }

  @ExceptionHandler({MethodArgumentNotValidException.class})
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ProblemDetail handleMethodArgumentNotValidException(
      Exception e, HttpServletRequest request) {
    ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    problemDetails.setType(
        URI.create("https://problems-registry.smartbear.com/invalid-body-property-value/"));
    problemDetails.setDetail("The request body contains an invalid body property value.");
    problemDetails.setTitle("Validation Error.");
    problemDetails.setInstance(URI.create(request.getContextPath()));
    Set<Error> errors = new HashSet<>();
    if (e instanceof WebExchangeBindException exception) {
      exception
          .getFieldErrors()
          .forEach(
              fieldError -> {
                Error error = new Error(fieldError.getDefaultMessage(), fieldError.getField());
                errors.add(error);
              });
    }
    problemDetails.setProperties(Collections.singletonMap("errors", errors));
    return problemDetails;
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ProblemDetail handleBadCredentialsException(
      BadCredentialsException e, HttpServletRequest request) {
    ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problemDetails.setType(URI.create("https://problems-registry.smartbear.com/unauthorized"));
    problemDetails.setTitle("Unauthorized");
    problemDetails.setDetail(e.getMessage());
    problemDetails.setInstance(URI.create(request.getContextPath()));
    return problemDetails;
  }

  @ExceptionHandler(InvalidEmailCodeException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ProblemDetail handleInvalidEmailCodeException(
      InvalidEmailCodeException e, HttpServletRequest request) {
    ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    problemDetails.setType(
        URI.create("https://problems-registry.smartbear.com/invalid-request-parameter-value"));
    problemDetails.setTitle("Invalid Email Code");
    problemDetails.setDetail(e.getMessage());
    problemDetails.setInstance(URI.create(request.getContextPath()));
    return problemDetails;
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ProblemDetail handleNotFoundException(NotFoundException e, HttpServletRequest request) {
    ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problemDetails.setType(URI.create("https://problems-registry.smartbear.com/not-found"));
    problemDetails.setTitle("Not Found");
    problemDetails.setDetail(e.getMessage());
    problemDetails.setInstance(URI.create(request.getRequestURI()));
    return problemDetails;
  }

  @ExceptionHandler(NotImplementedException.class)
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  public ProblemDetail handleNotImplementedException(
      NotImplementedException notImplementedException, HttpServletRequest request) {
    ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.NOT_IMPLEMENTED);
    problemDetails.setType(
        URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/501"));
    problemDetails.setTitle("Api does not implemented yet");
    problemDetails.setDetail(notImplementedException.getMessage());
    problemDetails.setInstance(URI.create(request.getRequestURI()));
    return problemDetails;
  }
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e, HttpServletRequest request) {
      ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
      problemDetails.setType(URI.create("https://problems-registry.smartbear.com/bad-request"));
      problemDetails.setTitle("Bad Request");
      problemDetails.setDetail(e.getMessage());
      problemDetails.setInstance(URI.create(request.getRequestURI()));
      return problemDetails;
    }
}
