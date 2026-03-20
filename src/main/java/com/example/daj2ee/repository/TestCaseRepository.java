package com.example.daj2ee.repository;

import com.example.daj2ee.entity.TestCase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for TestCase entity.
 * Provides access to test cases used for validating code submissions.
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
  /**
   * Finds all test cases for a specific problem, ordered by their sort order.
   *
   * @param problemId the ID of the problem
   * @return a list of test cases for the problem
   */
  List<TestCase> findByProblemIdOrderBySortOrderAsc(Long problemId);

  /**
   * Checks whether any test cases exist for the given problem.
   * Used by the data initializer to avoid adding duplicate test cases on restart.
   *
   * @param problemId the ID of the problem
   * @return true if at least one test case exists for the problem
   */
  boolean existsByProblemId(Long problemId);

  /**
   * Finds test cases for a specific problem filtered by their visibility.
   * Often used to return only "public" test cases to the frontend.
   *
   * @param problemId the ID of the problem
   * @param hidden whether the test case is hidden
   * @return a list of test cases matching the visibility criteria
   */
  List<TestCase> findByProblemIdAndHidden(Long problemId, boolean hidden);
}
