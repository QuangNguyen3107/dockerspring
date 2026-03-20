package com.example.daj2ee.repository;

import com.example.daj2ee.entity.UserSolvedProblem;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link UserSolvedProblem} entities.
 */
@Repository
public interface UserSolvedProblemRepository
  extends JpaRepository<UserSolvedProblem, Long> {
  /**
   * Check if a user has already solved a specific problem.
   */
  boolean existsByUserIdAndProblemId(Long userId, Long problemId);

  /**
   * Find all solved records for a given user.
   */
  List<UserSolvedProblem> findByUserId(Long userId);

  /**
   * Return just the problem IDs solved by a user — useful for bulk
   * "is solved?" lookups when rendering a problem list.
   */
  @Query(
    "SELECT usp.problem.id FROM UserSolvedProblem usp WHERE usp.user.id = :userId"
  )
  Set<Long> findSolvedProblemIdsByUserId(@Param("userId") Long userId);

  /**
   * Count how many distinct problems a user has solved.
   */
  long countByUserId(Long userId);

  /**
   * Count how many distinct users have solved a specific problem.
   */
  long countByProblemId(Long problemId);
}
