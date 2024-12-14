package com.github.ngodat0103.se347_backend.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

  public static String getUserIdFromAuthentication() {
    return (SecurityContextHolder.getContext().getAuthentication().getName());
  }
}
