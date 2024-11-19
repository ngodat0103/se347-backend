package com.github.ngodat0103.usersvc.persistence.repository;

import com.github.ngodat0103.usersvc.persistence.document.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<Account, String> {
  Mono<Account> findByEmail(String email);
}
