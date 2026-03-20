package com.example.daj2ee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Tracks which problems a user has successfully solved.
 *
 * Uses a regular surrogate PK (@Id + IDENTITY) with a unique constraint on
 * (user_id, problem_id) to prevent duplicate solved records while keeping
 * JPA operations simple.
 *
 * A row is inserted the first time a user gets allPassed=true on a problem.
 * Subsequent accepted submissions do NOT create new rows.
 */
@Entity
@Table(
  name = "user_solved_problems",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "uq_user_solved_problem",
      columnNames = { "user_id", "problem_id" }
    ),
  },
  indexes = {
    @Index(name = "idx_usp_user_id", columnList = "user_id"),
    @Index(name = "idx_usp_problem_id", columnList = "problem_id"),
  }
)
public class UserSolvedProblem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "problem_id", nullable = false)
  private Problem problem;

  /** Timestamp of the first accepted submission for this (user, problem) pair. */
  @Column(name = "solved_at", nullable = false)
  private LocalDateTime solvedAt;

  /** Judge0 language ID used in the accepted submission (nullable — for info only). */
  @Column(name = "language_id")
  private Integer languageId;

  public UserSolvedProblem() {}

  public UserSolvedProblem(User user, Problem problem, Integer languageId) {
    this.user = user;
    this.problem = problem;
    this.languageId = languageId;
    this.solvedAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    if (this.solvedAt == null) {
      this.solvedAt = LocalDateTime.now();
    }
  }

  // --- Getters & Setters ---

  public Long getId() {
    return id;
  }

  public UserSolvedProblem setId(Long id) {
    this.id = id;
    return this;
  }

  public User getUser() {
    return user;
  }

  public UserSolvedProblem setUser(User user) {
    this.user = user;
    return this;
  }

  public Problem getProblem() {
    return problem;
  }

  public UserSolvedProblem setProblem(Problem problem) {
    this.problem = problem;
    return this;
  }

  public LocalDateTime getSolvedAt() {
    return solvedAt;
  }

  public UserSolvedProblem setSolvedAt(LocalDateTime solvedAt) {
    this.solvedAt = solvedAt;
    return this;
  }

  public Integer getLanguageId() {
    return languageId;
  }

  public UserSolvedProblem setLanguageId(Integer languageId) {
    this.languageId = languageId;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserSolvedProblem that = (UserSolvedProblem) o;
    return (
      Objects.equals(
        user != null ? user.getId() : null,
        that.user != null ? that.user.getId() : null
      ) &&
      Objects.equals(
        problem != null ? problem.getId() : null,
        that.problem != null ? that.problem.getId() : null
      )
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      user != null ? user.getId() : null,
      problem != null ? problem.getId() : null
    );
  }

  @Override
  public String toString() {
    return (
      "UserSolvedProblem{" +
      "id=" + id +
      ", userId=" + (user != null ? user.getId() : null) +
      ", problemId=" + (problem != null ? problem.getId() : null) +
      ", solvedAt=" + solvedAt +
      ", languageId=" + languageId +
      '}'
    );
  }
}
