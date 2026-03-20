package com.example.daj2ee.dto.response;

import com.example.daj2ee.entity.Problem;
import com.example.daj2ee.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDto(
  Long id,
  String title,
  String description,
  String constraints,
  String difficulty,
  List<String> tags,
  Map<Integer, String> boilerplates,
  Long authorId,
  String authorUsername,
  boolean published,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  Boolean solved,
  Long solvedCount,
  List<TestCaseDto> testCases
) {
  public static ProblemDto fromEntity(Problem problem) {
    return fromEntity(problem, null, null, null);
  }

  public static ProblemDto fromEntity(
    Problem problem,
    Set<Long> solvedProblemIds,
    Long solvedCount,
    List<TestCaseDto> testCases
  ) {
    if (problem == null) return null;

    User author = problem.getAuthor();
    Long authorId = author != null ? author.getId() : null;
    String authorUsername = author != null ? author.getUsername() : null;

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

    return new ProblemDto(
      problem.getId(),
      problem.getTitle(),
      problem.getDescription(),
      problem.getConstraints(),
      problem.getDifficulty(),
      tagList,
      problem.getBoilerplates(),
      authorId,
      authorUsername,
      problem.isPublished(),
      problem.getCreatedAt(),
      problem.getUpdatedAt(),
      solved,
      solvedCount,
      testCases
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProblemDto that = (ProblemDto) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return (
      "ProblemDto{" +
      "id=" +
      id +
      ", title='" +
      title +
      '\'' +
      ", difficulty='" +
      difficulty +
      '\'' +
      ", authorUsername='" +
      authorUsername +
      '\'' +
      ", published=" +
      published +
      '}'
    );
  }
}
