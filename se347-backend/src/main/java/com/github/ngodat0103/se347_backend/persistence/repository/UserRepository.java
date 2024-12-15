package com.github.ngodat0103.se347_backend.persistence.repository;

import com.github.ngodat0103.se347_backend.persistence.document.user.User;
import com.github.ngodat0103.se347_backend.persistence.document.user.UserStatus;
import jakarta.validation.constraints.Email;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
  Optional<User> findByEmailAndUserStatus(
      @Email(message = "Email should be valid") String email, UserStatus userStatus);

  boolean existsByEmail(String email);
}
