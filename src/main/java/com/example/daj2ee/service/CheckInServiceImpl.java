package com.example.daj2ee.service;

import com.example.daj2ee.dto.response.CheckInResponse;
import com.example.daj2ee.entity.CheckIn;
import com.example.daj2ee.entity.User;
import com.example.daj2ee.repository.CheckInRepository;
import com.example.daj2ee.repository.UserRepository;
import com.example.daj2ee.security.UserPrincipal;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Default implementation of {@link CheckInService}.
 *
 * <p>
 * The server always determines the current date via {@link LocalDate#now()},
 * so no client-supplied timestamp is trusted.
 */
@Service
public class CheckInServiceImpl implements CheckInService {

  private static final int POINTS_PER_CHECKIN = 1;

  private final CheckInRepository checkInRepository;  
  private final UserRepository userRepository;

  public CheckInServiceImpl(
      CheckInRepository checkInRepository,
      UserRepository userRepository) {
    this.checkInRepository = checkInRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public CheckInResponse checkIn(UserPrincipal principal) {
    LocalDate today = LocalDate.now();

    if (checkInRepository.existsByUserIdAndCheckInDate(principal.getId(), today)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "You have already checked in today (" + today + ").");
    }

    User user = userRepository.findById(principal.getId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "User not found."));

    CheckIn checkIn = new CheckIn(user, today);
    checkInRepository.save(checkIn);

    user.setPoints(user.getPoints() + POINTS_PER_CHECKIN);
    userRepository.save(user);

    return new CheckInResponse(today, user.getPoints(), POINTS_PER_CHECKIN);
  }
}
