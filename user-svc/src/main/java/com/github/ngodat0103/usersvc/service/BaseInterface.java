package com.github.ngodat0103.usersvc.service;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface BaseInterface<DTO> {
  Mono<DTO> create(DTO dto, ServerHttpRequest request);

  Mono<DTO> update(DTO dto);

  Mono<DTO> delete(DTO dto);

  Mono<DTO> get(DTO dto);
}
