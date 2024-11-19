package com.github.ngodat0103.usersvc.service;

import reactor.core.publisher.Mono;

public interface BaseInterface<DTO> {
  Mono<DTO> create(DTO dto);

  Mono<DTO> update(DTO dto);

  Mono<DTO> delete(DTO dto);

  Mono<DTO> get(DTO dto);
}
