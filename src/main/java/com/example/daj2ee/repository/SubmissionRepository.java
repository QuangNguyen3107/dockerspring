package com.example.daj2ee.repository;

import com.example.daj2ee.entity.Submission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Submission} entities.
 *
 * Provides common queries used by the submission processing and API layers.
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
  /**
   * Find a submission by its Judge0 token.
   *
   * @param token the judge0 submission token
   * @return optional submission
   */
  Optional<Submission> findByToken(String token);

  /**
   * Find submissions for a specific user ordered by creation time (newest first).
   *
   * @param userId user id
   * @return list of submissions
   */
  List<Submission> findByUserIdOrderByCreatedAtDesc(Long userId);

  /**
   * Find submissions for a specific user with paging support.
   *
   * @param userId   user id
   * @param pageable paging parameters
   * @return page of submissions
   */
  Page<Submission> findByUserId(Long userId, Pageable pageable);

  /**
   * Find submissions whose status id is one of the provided values (useful to find pending submissions).
   *
   * @param statusIds collection of status ids
   * @return matching submissions
   */
  List<Submission> findByStatusIdIn(Collection<Integer> statusIds);

  /**
   * Find a bounded page of submissions whose status id is one of the provided values.
   * Use this overload in the poller to avoid loading an unbounded number of rows into memory.
   *
   * @param statusIds collection of status ids
   * @param pageable  paging parameters (use {@link PageRequest#of(int, int)} to cap the result)
   * @return bounded list of submissions
   */
  List<Submission> findByStatusIdIn(
    Collection<Integer> statusIds,
    Pageable pageable
  );

  /**
   * Find the most recent submission for a user.
   *
   * @param userId user id
   * @return optional most recent submission
   */
  Optional<Submission> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
