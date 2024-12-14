package com.github.ngodat0103.se347_backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@AllArgsConstructor
@Component
@Slf4j
public class BlackListTokenFilter extends OncePerRequestFilter {
  private final RedisTemplate<String, String> redisTemplate;
  private ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String jwtToken = authorizationHeader.substring(7); // Extract token after "Bearer "

    // Check if the token exists in Redis
    String result = redisTemplate.opsForValue().get(jwtToken);

    if (result != null) {
      // If token exists in Redis, deny access with a ProblemDetail response
      ProblemDetail problemDetail =
          ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "The token is blacklisted.");
      problemDetail.setTitle("Unauthorized Access");
      problemDetail.setInstance(URI.create(request.getRequestURI()));
      problemDetail.setType(
          URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/401"));
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
      response.getWriter().write(this.objectMapper.writeValueAsString(problemDetail));
      log.info("Request is deny, token is blacklisted");
      return;
    }

    // Proceed to the next filter in the chain
    filterChain.doFilter(request, response);
  }
}
