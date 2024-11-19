package com.github.ngodat0103.usersvc.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class AppController {
  @GetMapping(value = "/version", produces = "text/plain")
  Mono<String> getVersion() {
    return Mono.just(System.getenv("APP_VERSION"));
  }
}
