package com.example.daj2ee.service;

import com.example.daj2ee.dto.request.LoginRequest;
import com.example.daj2ee.dto.request.RegisterRequest;
import com.example.daj2ee.dto.response.LoginResponse;
import com.example.daj2ee.dto.response.UserDto;
import com.example.daj2ee.entity.User;
import com.example.daj2ee.repository.UserRepository;
import com.example.daj2ee.security.JwtTokenProvider;
import com.example.daj2ee.security.UserPrincipal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service that encapsulates authentication-related business logic.
 *
 * Responsibilities:
 * - registering a new user
 * - authenticating a user / issuing JWT token
 * - retrieving current authenticated user info
 *
 * This keeps the controller thin and focuses on translating service results into HTTP responses.
 */
@Service
public class AuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(
    AuthServiceImpl.class
  );

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;

  @Autowired
  public AuthServiceImpl(
    AuthenticationManager authenticationManager,
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtTokenProvider tokenProvider
  ) {
    this.authenticationManager = Objects.requireNonNull(
      authenticationManager,
      "authenticationManager must not be null"
    );
    this.userRepository = Objects.requireNonNull(
      userRepository,
      "userRepository must not be null"
    );
    this.passwordEncoder = Objects.requireNonNull(
      passwordEncoder,
      "passwordEncoder must not be null"
    );
    this.tokenProvider = Objects.requireNonNull(
      tokenProvider,
      "tokenProvider must not be null"
    );
  }

  /**
   * Register a new user.
   *
   * @throws ResponseStatusException 400 when request is invalid or username already exists
   */
  @Transactional
  public UserDto register(RegisterRequest request) {
    if (request == null) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Register request is required"
      );
    }

    String username =
      request.username() == null ? "" : request.username().trim();
    if (username.isBlank()) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Username is required"
      );
    }

    if (userRepository.existsByUsername(username)) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Username is already taken"
      );
    }

    String email = request.email() == null ? "" : request.email().trim();
    if (email.isBlank()) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Email is required"
      );
    }

    if (userRepository.existsByEmail(email)) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Email is already registered"
      );
    }

    User user = new User();
    user.setUsername(username);
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRoles("ROLE_USER"); // default role

    User saved = userRepository.save(user);
    log.info("Registered new user: {}", saved.getUsername());
    return UserDto.fromEntity(saved);
  }

  /**
   * Authenticate a user and return a login response containing a JWT token and metadata.
   *
   * @throws ResponseStatusException 401 when credentials are invalid
   */
  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    if (request == null) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Login request is required"
      );
    }

    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
          request.email(),
          request.password()
        )
      );
    } catch (Exception ex) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Invalid credentials"
      );
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);

    Object p = authentication.getPrincipal();
    UserPrincipal principal;
    if (p instanceof UserPrincipal) {
      principal = (UserPrincipal) p;
    } else {
      User user = userRepository
        .findByEmail(request.email())
        .orElseThrow(() ->
          new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid credentials"
          )
        );
      principal = UserPrincipal.create(user);
    }

    String token = tokenProvider.generateToken(principal);
    List<String> roles = principal
      .getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toList());

    log.info("User '{}' authenticated via email", principal.getUsername());
    return new LoginResponse(
      token,
      principal.getUsername(),
      roles,
      tokenProvider.getJwtExpirationMs()
    );
  }

  /**
   * Return information about the currently authenticated user.
   *
   * @throws ResponseStatusException 401 when not authenticated, 404 when user not found
   */
  @Transactional(readOnly = true)
  public UserDto getCurrentUser(UserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Not authenticated"
      );
    }
    User user = userRepository
      .findById(principal.getId())
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
      );

    return UserDto.fromEntity(user);
  }
}
