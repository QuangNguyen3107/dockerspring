package com.example.daj2ee.dto.response;

import com.example.daj2ee.entity.Problem;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemSummaryDto(
  Long id,
  String title,
  String difficulty,
  List<String> tags,
  String authorUsername,
  LocalDateTime createdAt,
  Boolean solved,
  Long solvedCount
) {
  public static ProblemSummaryDto fromEntity(Problem problem) {
    return fromEntity(problem, null);
  }

  public static ProblemSummaryDto fromEntity(
    Problem problem,
    Set<Long> solvedProblemIds
  ) {
    return fromEntity(problem, solvedProblemIds, null);
  }

  public static ProblemSummaryDto fromEntity(
    Problem problem,
    Set<Long> solvedProblemIds,
    Long solvedCount
  ) {
    if (problem == null) return null;

    String authorUsername =
      problem.getAuthor() != null ? problem.getAuthor().getUsername() : null;

    List<String> tagList = Collections.emptyList();
    if (problem.getTags() != null && !problem.getTags().isBlank()) {
      tagList = Arrays.stream(problem.getTags().split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
    }

    Boolean solved =
      solvedProblemIds != null
        ? solvedProblemIds.contains(problem.getId())
        : null;

    return new ProblemSummaryDto(
      problem.getId(),
      problem.getTitle(),
      problem.getDifficulty(),
      tagList,
      authorUsername,
      problem.getCreatedAt(),
      solved,
      solvedCount
    );
  }
}
