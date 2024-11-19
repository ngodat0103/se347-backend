package com.github.ngodat0103.usersvc.persistence.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.github.ngodat0103.usersvc.persistence.document.Account;

public interface UserRepository extends ReactiveMongoRepository<Account, String> {}
