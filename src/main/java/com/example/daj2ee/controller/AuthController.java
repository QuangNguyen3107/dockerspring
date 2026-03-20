package com.example.daj2ee.controller;

import com.example.daj2ee.dto.request.LoginRequest;
import com.example.daj2ee.dto.request.RegisterRequest;
import com.example.daj2ee.dto.response.BaseResponse;
import com.example.daj2ee.dto.response.LoginResponse;
import com.example.daj2ee.dto.response.UserDto;
import com.example.daj2ee.security.UserPrincipal;
import com.example.daj2ee.service.AuthServiceImpl;
import jakarta.validation.Valid;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(
    AuthController.class
  );

  private final AuthServiceImpl authService;

  public AuthController(AuthServiceImpl authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<BaseResponse<UserDto>> register(
    @Valid @RequestBody RegisterRequest request
  ) {
    try {
      UserDto created = authService.register(request);

      URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/api/users/{username}")
        .buildAndExpand(created.username())
        .toUri();

      return BaseResponse.<UserDto>createdEntity(created, location);
    } catch (ResponseStatusException e) {
      if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
        return BaseResponse.<UserDto>badRequestEntity(e.getReason());
      }
      log.error("Registration error", e);
      throw e;
    } catch (Exception e) {
      log.error("Error during registration", e);
      return BaseResponse.serverErrorEntity("Internal server error");
    }
  }

  @PostMapping("/login")
  public ResponseEntity<BaseResponse<LoginResponse>> login(
    @Valid @RequestBody LoginRequest request
  ) {
    try {
      LoginResponse response = authService.login(request);
      return BaseResponse.<LoginResponse>okEntity(response);
    } catch (ResponseStatusException e) {
      if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
        return BaseResponse.<LoginResponse>unauthorizedEntity(
          "Invalid credentials"
        );
      }
      log.error("Login error", e);
      throw e;
    } catch (Exception e) {
      log.error("Error during login", e);
      return BaseResponse.serverErrorEntity("Internal server error");
    }
  }

  @GetMapping("/current-user")
  public ResponseEntity<BaseResponse<UserDto>> me(
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    if (principal == null) {
      return BaseResponse.<UserDto>unauthorizedEntity("Not authenticated");
    }
    try {
      UserDto dto = authService.getCurrentUser(principal);
      return BaseResponse.<UserDto>okEntity(dto);
    } catch (ResponseStatusException e) {
      if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
        return BaseResponse.<UserDto>notFoundEntity("User not found");
      }
      log.error("Error getting current user", e);
      throw e;
    } catch (Exception e) {
      log.error("Error while getting current user", e);
      return BaseResponse.serverErrorEntity("Internal server error");
    }
  }
}
