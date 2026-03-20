package com.example.daj2ee.repository;

import com.example.daj2ee.entity.Problem;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Problem entity.
 */
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
  /**
   * Checks whether a problem with the given title already exists.
   * Used by the data initializer for idempotent per-problem seeding.
   *
   * @param title the problem title to look up
   * @return true if a problem with that title exists
   */
  boolean existsByTitle(String title);

  /**
   * Finds a problem by its exact title.
   * Used by the data initializer to retrieve an already-seeded problem.
   *
   * @param title the problem title to look up
   * @return an Optional containing the problem if found
   */
  java.util.Optional<Problem> findByTitle(String title);

  /**
   * Finds published problems with pagination support, eagerly fetching the author
   * via @EntityGraph instead of JOIN FETCH.
   *
   * Using JOIN FETCH with Pageable triggers Hibernate's HHH90003004 warning:
   * it cannot apply LIMIT/OFFSET at SQL level when a collection fetch is present,
   * so it pulls ALL rows into memory and paginates in Java. @EntityGraph avoids
   * this entirely — Spring Data issues a proper paginated query first, then a
   * separate batch fetch for the author associations.
   *
   * @param pageable paging and sorting parameters
   * @return a page of published problems with authors pre-loaded
   */
  @EntityGraph(attributePaths = { "author" })
  Page<Problem> findByPublishedTrue(Pageable pageable);

  /**
   * Finds all problems created by a specific user.
   *
   * @param username the username of the author
   * @return a list of problems by the author
   */
  List<Problem> findByAuthorUsername(String username);

  /**
   * Finds published problems by difficulty level.
   *
   * @param difficulty the difficulty level (e.g., EASY, MEDIUM, HARD)
   * @return a list of published problems with the given difficulty
   */
  List<Problem> findByPublishedTrueAndDifficulty(String difficulty);
}
