package com.github.ngodat0103.usersvc.exception;

import java.security.Principal;
import org.slf4j.Logger;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

public final class Util {
  private static final String TEMPLATE_NOT_FOUND = "%s with %s: %s not found";
  private static final String TEMPLATE_CONFLICT = "%s with %s: %s already exists";

  private Util() {
    throw new IllegalStateException("Utility class");
  }

  public static Mono<String> getUserIdFromAuthentication() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Principal::getName);
  }

  public static ConflictException createConflictException(
      Logger log, String entity, String attributeName, Object attributeValues) {
    String message = String.format(TEMPLATE_CONFLICT, entity, attributeName, attributeValues);
    logging(log, message, null);
    return new ConflictException(message, ConflictException.Type.ALREADY_EXISTS);
  }

  public static NotFoundException createNotFoundException(
      Logger log, String entity, String attributeName, Object attributeValues) {
    String message = String.format(TEMPLATE_NOT_FOUND, entity, attributeName, attributeValues);
    logging(log, message, null);
    throw new NotFoundException(message);
  }

  private static void logging(Logger log, String message, Exception exception) {
    if (log.isTraceEnabled()) {
      log.debug(message, exception);
    } else if (log.isDebugEnabled()) {
      log.debug(message);
    }
  }
}
