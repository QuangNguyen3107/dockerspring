package com.example.daj2ee.service;

import com.example.daj2ee.dto.request.LoginRequest;
import com.example.daj2ee.dto.request.RegisterRequest;
import com.example.daj2ee.dto.response.LoginResponse;
import com.example.daj2ee.dto.response.UserDto;
import com.example.daj2ee.security.UserPrincipal;

/**
 * Contract for authentication-related business operations.
 *
 * Implementations encapsulate rules for:
 * - registering new users
 * - authenticating users and issuing tokens
 * - retrieving information about the currently authenticated user
 */
public interface AuthService {
  /**
   * Register a new user and return the created user DTO.
   *
   * @param request registration payload
   * @return created user representation
   */
  UserDto register(RegisterRequest request);

  /**
   * Authenticate a user and return a login response containing a JWT token and metadata.
   *
   * @param request login payload
   * @return login response (token, username, roles, expiration)
   */
  LoginResponse login(LoginRequest request);

  /**
   * Return basic information about the currently authenticated user.
   *
   * @param principal authenticated user principal
   * @return current user DTO
   */
  UserDto getCurrentUser(UserPrincipal principal);
}
