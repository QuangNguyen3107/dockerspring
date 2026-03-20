package com.example.daj2ee.service;

import com.example.daj2ee.dto.response.CheckInResponse;
import com.example.daj2ee.security.UserPrincipal;

/**
 * Contract for the daily check-in feature.
 */
public interface CheckInService {
  CheckInResponse checkIn(UserPrincipal principal);
}
