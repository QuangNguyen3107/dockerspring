package com.example.daj2ee.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing a user's daily check-in for the "Daily Algorithm Journey"
 * feature.
 * Each record indicates that a user has checked in on a specific calendar date.
 * The combination of user and check-in date is unique to prevent multiple
 * check-ins on the same day.
 */
@Entity
@Table(name = "check_ins", uniqueConstraints = {
    @UniqueConstraint(name = "uq_checkin_user_date", columnNames = { "user_id", "check_in_date" })
}, indexes = {
    @Index(name = "idx_checkin_user_id", columnList = "user_id"),
    @Index(name = "idx_checkin_date", columnList = "check_in_date")
})
public class CheckIn {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "check_in_date", nullable = false)
  private LocalDate checkInDate;

  public CheckIn() {
  }

  public CheckIn(User user, LocalDate checkInDate) {
    this.user = user;
    this.checkInDate = checkInDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDate getCheckInDate() {
    return checkInDate;
  }

  public void setCheckInDate(LocalDate checkInDate) {
    this.checkInDate = checkInDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof CheckIn))
      return false;
    CheckIn checkIn = (CheckIn) o;
    return Objects.equals(id, checkIn.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
