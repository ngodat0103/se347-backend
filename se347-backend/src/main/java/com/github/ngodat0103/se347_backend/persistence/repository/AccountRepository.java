package com.github.ngodat0103.se347_backend.persistence.repository;

import com.github.ngodat0103.se347_backend.persistence.document.account.Account;
import com.github.ngodat0103.se347_backend.persistence.document.account.AccountStatus;
import jakarta.validation.constraints.Email;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {
  Optional<Account> findByEmailAndAccountStatus(
      @Email(message = "Email should be valid") String email, AccountStatus accountStatus);

  boolean existsByEmail(String email);
}
