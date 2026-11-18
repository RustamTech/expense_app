package com.example.notes.application.coolproject.repository;

import com.example.notes.application.coolproject.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for user accounts. Emails are stored normalized (trimmed, lower-case). */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  /**
   * Finds a user by normalized email.
   *
   * @param email normalized email
   * @return the user, or empty if none exists
   */
  Optional<UserEntity> findByEmail(String email);

  /**
   * Checks whether the normalized email is already registered.
   *
   * @param email normalized email
   * @return {@code true} if taken
   */
  boolean existsByEmail(String email);
}
