package com.github.ngodat0103.usersvc.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class BodyRequestSizeLimitFilter implements WebFilter {
  private static final int MAX_UPLOAD_SIZE = 2 * 1024 * 1024; // 2MB

  private ObjectMapper objectMapper;

  @Override
  public @NotNull Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
    long length = exchange.getRequest().getHeaders().getContentLength();
    if (length > MAX_UPLOAD_SIZE) {
      ServerHttpResponse response = exchange.getResponse();
      response.setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
      var headers = response.getHeaders();
      headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
      exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
      return exchange.getResponse().writeWith(writeProblemDetail(exchange));
    }
    return chain.filter(exchange);
  }

  private Flux<DataBuffer> writeProblemDetail(ServerWebExchange exchange) {
    ProblemDetail problemDetail = createProblemDetail();
    byte[] bytes = new byte[0];
    try {
      bytes = objectMapper.writeValueAsBytes(problemDetail);
    } catch (Exception e) {
      log.error("Failed to write problem detail", e);
    }
    DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return Flux.just(dataBuffer);
  }

  private ProblemDetail createProblemDetail() {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.PAYLOAD_TOO_LARGE);
    problemDetail.setDetail("Request body size should not exceed " + MAX_UPLOAD_SIZE + " bytes");
    problemDetail.setInstance(null);
    problemDetail.setTitle("Request body size exceeded");
    problemDetail.setType(
        URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/413"));
    return problemDetail;
  }
}
