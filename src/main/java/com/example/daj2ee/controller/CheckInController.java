package com.example.daj2ee.controller;

import com.example.daj2ee.dto.response.BaseResponse;
import com.example.daj2ee.dto.response.CheckInResponse;
import com.example.daj2ee.security.UserPrincipal;
import com.example.daj2ee.service.CheckInService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/checkin")
@CrossOrigin(origins = "*")
public class CheckInController {

  private static final Logger log = LoggerFactory.getLogger(
    CheckInController.class
  );

  private final CheckInService checkInService;

  public CheckInController(CheckInService checkInService) {
    this.checkInService = checkInService;
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<BaseResponse<CheckInResponse>> checkIn(
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      CheckInResponse result = checkInService.checkIn(principal);
      String message =
        "Check-in successful! You earned " +
        result.pointsEarned() +
        " point(s).";
      return ResponseEntity.ok(BaseResponse.ok(message, result));
    } catch (ResponseStatusException e) {
      if (e.getStatusCode() == HttpStatus.CONFLICT) {
        log.warn("Check-in conflict: {}", e.getReason());
        return BaseResponse.<CheckInResponse>conflictEntity(e.getReason());
      }
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return BaseResponse.<CheckInResponse>notFoundEntity(e.getReason());
      }
      log.error("Check-in error", e);
      throw e;
    } catch (Exception e) {
      log.error("Error during check-in", e);
      return BaseResponse.serverErrorEntity("Internal server error");
    }
  }
}
