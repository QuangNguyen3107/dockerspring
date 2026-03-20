package com.example.daj2ee.controller;

import com.example.daj2ee.dto.request.RedeemRequest;
import com.example.daj2ee.dto.request.UpdateUserProfileRequest;
import com.example.daj2ee.dto.response.BaseResponse;
import com.example.daj2ee.dto.response.RedeemResponse;
import com.example.daj2ee.dto.response.UserDto;
import com.example.daj2ee.security.UserPrincipal;
import com.example.daj2ee.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(
    UserController.class
  );

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/redeem")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<BaseResponse<RedeemResponse>> redeemPoints(
    @AuthenticationPrincipal UserPrincipal principal,
    @Valid @RequestBody RedeemRequest request
  ) {
    try {
      RedeemResponse response = userService.redeemPoints(principal, request);
      return BaseResponse.okEntity(response, "Redeem successful");
    } catch (IllegalArgumentException e) {
      log.warn("Redeem failed: {}", e.getMessage());
      return BaseResponse.badRequestEntity(e.getMessage());
    } catch (Exception e) {
      log.error("Error during redeem", e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @PutMapping("/profile")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<BaseResponse<UserDto>> updateProfile(
    @AuthenticationPrincipal UserPrincipal principal,
    @Valid @RequestBody UpdateUserProfileRequest request
  ) {
    try {
      UserDto response = userService.updateProfile(principal, request);
      return BaseResponse.okEntity(response, "Profile updated successfully");
    } catch (ResponseStatusException e) {
      if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
        return BaseResponse.badRequestEntity(e.getReason());
      }
      if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
        return BaseResponse.unauthorizedEntity(e.getReason());
      }
      if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
        return BaseResponse.notFoundEntity(e.getReason());
      }
      throw e;
    } catch (Exception e) {
      log.error("Error updating user profile", e);
      return BaseResponse.serverErrorEntity("Internal server error");
    }
  }
}
