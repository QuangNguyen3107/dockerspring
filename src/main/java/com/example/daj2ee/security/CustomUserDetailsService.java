package com.example.daj2ee.security;

import com.example.daj2ee.repository.UserRepository;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that loads users from the database and adapts them to Spring Security's {@link UserDetails}.
 *
 * - Primary method {@link #loadUserByUsername(String)} is used by Spring Security during authentication.
 * - Convenience method {@link #loadUserById(Long)} can be used by other components that need to resolve a user by id.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private static final Logger logger = LoggerFactory.getLogger(
    CustomUserDetailsService.class
  );

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = Objects.requireNonNull(
      userRepository,
      "userRepository must not be null"
    );
  }

  /**
   * Load a user by email and convert it to a {@link UserPrincipal}.
   *
   * Spring Security calls this method with whatever string the user supplies as
   * their "username" — we treat that string as an email address.
   *
   * @param email the email identifying the user whose data is required.
   * @return a fully populated {@link UserDetails} record (never {@code null})
   * @throws UsernameNotFoundException if no user with that email exists
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email)
    throws UsernameNotFoundException {
    logger.debug("Loading user by email: {}", email);
    return userRepository
      .findByEmail(email)
      .map(UserPrincipal::create)
      .orElseThrow(() -> {
        String msg = "User not found with email: " + email;
        logger.debug(msg);
        return new UsernameNotFoundException(msg);
      });
  }

  /**
   * Convenience method to load a user by id. Returns a {@link UserPrincipal} if found.
   *
   * @param id the user's id
   * @return a {@link UserDetails} for the user
   * @throws UsernameNotFoundException if the user is not found
   */
  @Transactional(readOnly = true)
  public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
    logger.debug("Loading user by id: {}", id);
    return userRepository
      .findById(id)
      .map(UserPrincipal::create)
      .orElseThrow(() -> {
        String msg = "User not found with id: " + id;
        logger.debug(msg);
        return new UsernameNotFoundException(msg);
      });
  }
}
