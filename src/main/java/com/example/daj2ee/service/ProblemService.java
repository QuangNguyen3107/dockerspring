package com.example.daj2ee.service;

import com.example.daj2ee.dto.response.PaginatedReponse;
import com.example.daj2ee.dto.response.ProblemDto;
import com.example.daj2ee.dto.response.ProblemSummaryDto;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contract for problem-related business operations.
 * This service replaces the TutorialService and handles the lifecycle of coding challenges.
 */
public interface ProblemService {
  /**
   * Return all problems currently stored in the system.
   *
   * @return a list of all problem DTOs
   */
  List<ProblemSummaryDto> findAll(Long userId);

  /**
   * Return a paginated list of published problems.
   *
   * @param page zero-based page index
   * @param size number of items per page
   * @return paginated response containing the problems for the requested page
   */
  /**
   * @param userId optional authenticated user ID — when provided, each item
   *               will include a {@code solved} flag indicating whether that
   *               user has solved the problem. Pass null for unauthenticated requests.
   */
  PaginatedReponse<List<ProblemSummaryDto>> findPublished(
    int page,
    int size,
    Long userId
  );

  /**
   * Find published problems filtered by difficulty.
   *
   * @param difficulty the difficulty level (e.g., "EASY", "MEDIUM", "HARD")
   * @return a list of problems matching the difficulty
   */
  /**
   * @param userId optional authenticated user ID for solved tracking. Pass null if unauthenticated.
   */
  List<ProblemSummaryDto> findByDifficulty(String difficulty, Long userId);

  /**
   * Mark a problem as solved by a user. Idempotent — calling multiple times has no effect.
   *
   * @param userId     the user who solved the problem
   * @param problemId  the problem that was solved
   * @param languageId the Judge0 language ID used (nullable)
   */
  void markSolved(Long userId, Long problemId, Integer languageId);

  /**
   * Return the set of problem IDs solved by the given user.
   */
  Set<Long> getSolvedProblemIds(Long userId);

  /**
   * Get a single problem by its ID.
   *
   * @param id     the problem ID
   * @param userId optional authenticated user ID for solved tracking. Pass null if unauthenticated.
   * @return the problem DTO, or null if not found
   */
  ProblemDto getById(Long id, Long userId);

  /**
   * Return the number of distinct users who have solved a given problem.
   *
   * @param problemId the problem ID
   * @return count of solvers
   */
  long getSolvedCount(Long problemId);

  /**
   * Create a new problem authored by the specified user.
   *
   * @param dto the problem data
   * @param authorUsername the username of the author creating the problem
   * @return the created problem DTO
   */
  ProblemDto create(ProblemDto dto, String authorUsername);

  /**
   * Update an existing problem. Only the author or an admin should be allowed to update.
   *
   * @param id the problem ID to update
   * @param dto the updated data
   * @param requesterUsername the username of the person requesting the update
   * @return the updated problem DTO
   */
  ProblemDto update(Long id, ProblemDto dto, String requesterUsername);

  /**
   * Delete a problem from the system.
   *
   * @param id the problem ID to delete
   * @param requesterUsername the username of the person requesting the deletion
   */
  void delete(Long id, String requesterUsername);
}
