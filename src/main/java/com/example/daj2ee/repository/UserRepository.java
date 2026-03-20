package com.example.daj2ee.repository;

import com.example.daj2ee.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link User} entities.
 *
 * Provides basic lookup methods used by authentication and user management services.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  /**
   * Find a user by their username.
   *
   * @param username the username to search for
   * @return an Optional containing the user if found
   */
  Optional<User> findByUsername(String username);

  /**
   * Check whether a username already exists.
   *
   * @param username the username to check
   * @return true if a user with the given username exists
   */
  boolean existsByUsername(String username);

  /**
   * Optionally find a user by their email address.
   *
   * @param email the email to search for
   * @return an Optional containing the user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Check whether an email address is already registered.
   *
   * @param email the email to check
   * @return true if a user with the given email exists
   */
  boolean existsByEmail(String email);
}
