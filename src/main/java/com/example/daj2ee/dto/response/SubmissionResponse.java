package com.example.daj2ee.dto.response;

import com.example.daj2ee.entity.Submission;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubmissionResponse(
  Long id,
  String token,
  Long problemId,
  Integer languageId,
  Integer statusId,
  String statusDescription,
  String stdout,
  String stderr,
  String compileOutput,
  String message,
  String time,
  Long memory,
  String username,
  List<TestCaseResult> testCaseResults,
  Boolean allPassed,
  Boolean firstSolve,
  LocalDateTime createdAt,
  LocalDateTime finishedAt
) {
  public static SubmissionResponse fromEntity(Submission s) {
    if (s == null) return null;

    return new SubmissionResponse(
      s.getId(),
      s.getToken(),
      s.getProblem() != null ? s.getProblem().getId() : null,
      s.getLanguageId(),
      s.getStatusId(),
      s.getStatusDescription(),
      s.getStdout(),
      s.getStderr(),
      s.getCompileOutput(),
      s.getMessage(),
      s.getTime(),
      s.getMemory(),
      s.getUser() != null ? s.getUser().getUsername() : null,
      null,
      null,
      null,
      s.getCreatedAt(),
      s.getFinishedAt()
    );
  }

  @Override
  public String toString() {
    return (
      "SubmissionResponse{" +
      "id=" +
      id +
      ", token='" +
      token +
      '\'' +
      ", problemId=" +
      problemId +
      ", languageId=" +
      languageId +
      ", statusId=" +
      statusId +
      ", statusDescription='" +
      statusDescription +
      '\'' +
      ", stdout=" +
      (stdout != null ? "[present]" : "null") +
      ", stderr=" +
      (stderr != null ? "[present]" : "null") +
      ", compileOutput=" +
      (compileOutput != null ? "[present]" : "null") +
      ", time='" +
      time +
      '\'' +
      ", memory=" +
      memory +
      ", username='" +
      username +
      '\'' +
      ", allPassed=" +
      allPassed +
      ", firstSolve=" +
      firstSolve +
      ", testCaseResults=" +
      (testCaseResults != null
        ? testCaseResults.size() + " result(s)"
        : "null") +
      ", createdAt=" +
      createdAt +
      ", finishedAt=" +
      finishedAt +
      '}'
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SubmissionResponse that)) return false;
    return Objects.equals(id, that.id) && Objects.equals(token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, token);
  }
}
