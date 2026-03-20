package com.example.daj2ee.service;

import com.example.daj2ee.dto.response.PaginatedReponse;
import com.example.daj2ee.dto.response.ProblemDto;
import com.example.daj2ee.dto.response.ProblemSummaryDto;
import com.example.daj2ee.dto.response.TestCaseDto;
import com.example.daj2ee.entity.Problem;
import com.example.daj2ee.entity.User;
import com.example.daj2ee.entity.UserSolvedProblem;
import com.example.daj2ee.repository.ProblemRepository;
import com.example.daj2ee.repository.TestCaseRepository;
import com.example.daj2ee.repository.UserRepository;
import com.example.daj2ee.repository.UserSolvedProblemRepository;
import com.example.daj2ee.util.shared.PaginationConstants;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ProblemService that manages the lifecycle of coding challenges.
 * Replaces TutorialServiceImpl.
 */
@Service
public class ProblemServiceImpl implements ProblemService {

  private final ProblemRepository problemRepository;
  private final UserRepository userRepository;
  private final UserSolvedProblemRepository userSolvedProblemRepository;
  private final TestCaseRepository testCaseRepository;

  public ProblemServiceImpl(
    ProblemRepository problemRepository,
    UserRepository userRepository,
    UserSolvedProblemRepository userSolvedProblemRepository,
    TestCaseRepository testCaseRepository
  ) {
    this.problemRepository = problemRepository;
    this.userRepository = userRepository;
    this.userSolvedProblemRepository = userSolvedProblemRepository;
    this.testCaseRepository = testCaseRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProblemSummaryDto> findAll(Long userId) {
    Set<Long> solvedIds =
      userId != null
        ? userSolvedProblemRepository.findSolvedProblemIdsByUserId(userId)
        : null;
    return problemRepository
      .findAll()
      .stream()
      .map(p ->
        ProblemSummaryDto.fromEntity(
          p,
          solvedIds,
          userSolvedProblemRepository.countByProblemId(p.getId())
        )
      )
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PaginatedReponse<List<ProblemSummaryDto>> findPublished(
    int page,
    int size,
    Long userId
  ) {
    int clampedSize = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
    Pageable pageable = PageRequest.of(
      page,
      clampedSize,
      Sort.by(Sort.Direction.DESC, PaginationConstants.DEFAULT_SORT_FIELD)
    );

    Page<Problem> result = problemRepository.findByPublishedTrue(pageable);

    Set<Long> solvedIds =
      userId != null
        ? userSolvedProblemRepository.findSolvedProblemIdsByUserId(userId)
        : null;

    List<ProblemSummaryDto> items = result
      .getContent()
      .stream()
      .map(p ->
        ProblemSummaryDto.fromEntity(
          p,
          solvedIds,
          userSolvedProblemRepository.countByProblemId(p.getId())
        )
      )
      .collect(Collectors.toList());

    return new PaginatedReponse<>(
      result.getTotalElements(),
      result.getTotalPages(),
      result.getNumber() + 1, // convert to 1-based for the response
      result.getSize(),
      items
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProblemSummaryDto> findByDifficulty(
    String difficulty,
    Long userId
  ) {
    Set<Long> solvedIds =
      userId != null
        ? userSolvedProblemRepository.findSolvedProblemIdsByUserId(userId)
        : null;
    return problemRepository
      .findByPublishedTrueAndDifficulty(difficulty)
      .stream()
      .map(p ->
        ProblemSummaryDto.fromEntity(
          p,
          solvedIds,
          userSolvedProblemRepository.countByProblemId(p.getId())
        )
      )
      .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void markSolved(Long userId, Long problemId, Integer languageId) {
    // Idempotent — do nothing if already solved
    if (
      userSolvedProblemRepository.existsByUserIdAndProblemId(userId, problemId)
    ) {
      return;
    }
    User user = userRepository.findById(userId).orElse(null);
    Problem problem = problemRepository.findById(problemId).orElse(null);
    if (user == null || problem == null) {
      return;
    }
    userSolvedProblemRepository.save(
      new UserSolvedProblem(user, problem, languageId)
    );
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Long> getSolvedProblemIds(Long userId) {
    return userSolvedProblemRepository.findSolvedProblemIdsByUserId(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public ProblemDto getById(Long id, Long userId) {
    return problemRepository
      .findById(id)
      .map(problem -> {
        Set<Long> solvedIds =
          userId != null
            ? userSolvedProblemRepository.findSolvedProblemIdsByUserId(userId)
            : null;
        long solvedCount = userSolvedProblemRepository.countByProblemId(
          problem.getId()
        );
        List<TestCaseDto> testCases = testCaseRepository
          .findByProblemIdOrderBySortOrderAsc(problem.getId())
          .stream()
          .map(TestCaseDto::fromEntity)
          .collect(Collectors.toList());
        return ProblemDto.fromEntity(
          problem,
          solvedIds,
          solvedCount,
          testCases
        );
      })
      .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public long getSolvedCount(Long problemId) {
    return userSolvedProblemRepository.countByProblemId(problemId);
  }

  @Override
  @Transactional
  public ProblemDto create(ProblemDto dto, String authorUsername) {
    User author = userRepository
      .findByUsername(authorUsername)
      .orElseThrow(() ->
        new RuntimeException("User not found: " + authorUsername)
      );

    Problem problem = new Problem();
    copyDtoToEntity(dto, problem);
    problem.setAuthor(author);

    Problem saved = problemRepository.save(problem);
    List<TestCaseDto> testCases = testCaseRepository
      .findByProblemIdOrderBySortOrderAsc(saved.getId())
      .stream()
      .map(TestCaseDto::fromEntity)
      .collect(Collectors.toList());
    return ProblemDto.fromEntity(saved, null, 0L, testCases);
  }

  @Override
  @Transactional
  public ProblemDto update(Long id, ProblemDto dto, String requesterUsername) {
    Problem problem = problemRepository
      .findById(id)
      .orElseThrow(() ->
        new RuntimeException("Problem not found with id: " + id)
      );

    User requester = userRepository
      .findByUsername(requesterUsername)
      .orElseThrow(() ->
        new RuntimeException("User not found: " + requesterUsername)
      );

    // Authorization: Only author or admin can update
    boolean isAdmin =
      requester.getRoles() != null &&
      requester.getRoles().contains("ROLE_ADMIN");
    boolean isAuthor =
      problem.getAuthor() != null &&
      problem.getAuthor().getUsername().equals(requesterUsername);

    if (!isAdmin && !isAuthor) {
      throw new RuntimeException("Not authorized to update this problem");
    }

    copyDtoToEntity(dto, problem);
    Problem updated = problemRepository.save(problem);
    long solvedCount = userSolvedProblemRepository.countByProblemId(
      updated.getId()
    );
    List<TestCaseDto> testCases = testCaseRepository
      .findByProblemIdOrderBySortOrderAsc(updated.getId())
      .stream()
      .map(TestCaseDto::fromEntity)
      .collect(Collectors.toList());
    return ProblemDto.fromEntity(updated, null, solvedCount, testCases);
  }

  @Override
  @Transactional
  public void delete(Long id, String requesterUsername) {
    Problem problem = problemRepository
      .findById(id)
      .orElseThrow(() ->
        new RuntimeException("Problem not found with id: " + id)
      );

    User requester = userRepository
      .findByUsername(requesterUsername)
      .orElseThrow(() ->
        new RuntimeException("User not found: " + requesterUsername)
      );

    // Authorization: Only author or admin can delete
    boolean isAdmin =
      requester.getRoles() != null &&
      requester.getRoles().contains("ROLE_ADMIN");
    boolean isAuthor =
      problem.getAuthor() != null &&
      problem.getAuthor().getUsername().equals(requesterUsername);

    if (!isAdmin && !isAuthor) {
      throw new RuntimeException("Not authorized to delete this problem");
    }

    problemRepository.delete(problem);
  }

  private void copyDtoToEntity(ProblemDto dto, Problem entity) {
    entity.setTitle(dto.title());
    entity.setDescription(dto.description());
    entity.setConstraints(dto.constraints());
    entity.setDifficulty(dto.difficulty());
    entity.setBoilerplates(dto.boilerplates());
    entity.setPublished(dto.published());

    if (dto.tags() != null) {
      entity.setTags(String.join(",", dto.tags()));
    }
  }
}
