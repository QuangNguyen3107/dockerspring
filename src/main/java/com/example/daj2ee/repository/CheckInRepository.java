package com.example.daj2ee.repository;

import com.example.daj2ee.entity.CheckIn;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link CheckIn} entities.
 */
@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
  boolean existsByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);
}
