package com.example.daj2ee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a code submission to Judge0 (or another code execution engine).
 *
 * Stores the submission token returned by Judge0 and the rich results (stdout, stderr, compile output,
 * status, execution time and memory). This entity is designed to support both synchronous and asynchronous
 * submission flows (create + poll).
 */
@Entity
@Table(
  name = "submissions",
  indexes = {
    @Index(name = "idx_submission_token", columnList = "token"),
    @Index(name = "idx_submission_user", columnList = "user_id"),
    @Index(name = "idx_submission_problem", columnList = "problem_id"),
  }
)
public class Submission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The unique token returned by Judge0 for this submission (used to query status/results).
   */
  @Column(unique = true)
  private String token;

  /**
   * The user who submitted the code (may be null for anonymous submissions).
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  /**
   * The problem this submission is attempting to solve.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "problem_id")
  private Problem problem;

  /**
   * Judge0 language id (e.g. 71 for Python 3).
   */
  @Column(name = "language_id")
  private Integer languageId;

  /**
   * The source code submitted.
   */
  @Lob
  @Column(name = "source_code", columnDefinition = "TEXT")
  private String sourceCode;

  /**
   * Optional standard input provided for the program.
   */
  @Lob
  @Column(name = "stdin", columnDefinition = "TEXT")
  private String stdin;

  /**
   * Captured standard output (if available).
   */
  @Lob
  @Column(name = "stdout", columnDefinition = "TEXT")
  private String stdout;

  /**
   * Captured standard error (if available).
   */
  @Lob
  @Column(name = "stderr", columnDefinition = "TEXT")
  private String stderr;

  /**
   * Compilation output (if any).
   */
  @Lob
  @Column(name = "compile_output", columnDefinition = "TEXT")
  private String compileOutput;

  /**
   * Additional message (e.g., exit_code information or other runner messages).
   */
  @Lob
  @Column(name = "message", columnDefinition = "TEXT")
  private String message;

  /**
   * Status id returned by Judge0:
   * 1 = In Queue, 2 = Processing, >2 = finished states (Accepted, Wrong Answer, etc.)
   */
  @Column(name = "status_id")
  private Integer statusId;

  /**
   * Human-readable status description returned by Judge0 (e.g. \"Accepted\").
   */
  @Column(name = "status_description")
  private String statusDescription;

  /**
   * Execution time as reported by Judge0 (string, e.g. \"0.001\").
   */
  @Column(name = "time")
  private String time;

  /**
   * Memory usage in kilobytes.
   */
  @Column(name = "memory")
  private Long memory;

  /**
   * Raw JSON response from Judge0 (for debugging/audit).
   */
  @Lob
  @Column(name = "raw_response", columnDefinition = "TEXT")
  private String rawResponse;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "finished_at")
  private LocalDateTime finishedAt;

  public Submission() {
    // default constructor for JPA
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    if (this.statusId != null && this.statusId > 2 && this.finishedAt == null) {
      this.finishedAt = LocalDateTime.now();
    }
  }

  // --- Getters and setters ---

  public Long getId() {
    return id;
  }

  public Submission setId(Long id) {
    this.id = id;
    return this;
  }

  public String getToken() {
    return token;
  }

  public Submission setToken(String token) {
    this.token = token;
    return this;
  }

  public User getUser() {
    return user;
  }

  public Submission setUser(User user) {
    this.user = user;
    return this;
  }

  public Problem getProblem() {
    return problem;
  }

  public Submission setProblem(Problem problem) {
    this.problem = problem;
    return this;
  }

  public Integer getLanguageId() {
    return languageId;
  }

  public Submission setLanguageId(Integer languageId) {
    this.languageId = languageId;
    return this;
  }

  public String getSourceCode() {
    return sourceCode;
  }

  public Submission setSourceCode(String sourceCode) {
    this.sourceCode = sourceCode;
    return this;
  }

  public String getStdin() {
    return stdin;
  }

  public Submission setStdin(String stdin) {
    this.stdin = stdin;
    return this;
  }

  public String getStdout() {
    return stdout;
  }

  public Submission setStdout(String stdout) {
    this.stdout = stdout;
    return this;
  }

  public String getStderr() {
    return stderr;
  }

  public Submission setStderr(String stderr) {
    this.stderr = stderr;
    return this;
  }

  public String getCompileOutput() {
    return compileOutput;
  }

  public Submission setCompileOutput(String compileOutput) {
    this.compileOutput = compileOutput;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public Submission setMessage(String message) {
    this.message = message;
    return this;
  }

  public Integer getStatusId() {
    return statusId;
  }

  public Submission setStatusId(Integer statusId) {
    this.statusId = statusId;
    return this;
  }

  public String getStatusDescription() {
    return statusDescription;
  }

  public Submission setStatusDescription(String statusDescription) {
    this.statusDescription = statusDescription;
    return this;
  }

  public String getTime() {
    return time;
  }

  public Submission setTime(String time) {
    this.time = time;
    return this;
  }

  public Long getMemory() {
    return memory;
  }

  public Submission setMemory(Long memory) {
    this.memory = memory;
    return this;
  }

  public String getRawResponse() {
    return rawResponse;
  }

  public Submission setRawResponse(String rawResponse) {
    this.rawResponse = rawResponse;
    return this;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Submission setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public LocalDateTime getFinishedAt() {
    return finishedAt;
  }

  public Submission setFinishedAt(LocalDateTime finishedAt) {
    this.finishedAt = finishedAt;
    return this;
  }

  // --- Convenience methods ---

  /**
   * Whether this submission has finished execution based on Judge0 status semantics.
   * Judge0 statuses: 1 (In Queue), 2 (Processing), >2 completed states.
   */
  public boolean isFinished() {
    return statusId != null && statusId > 2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Submission that = (Submission) o;
    return Objects.equals(id, that.id) && Objects.equals(token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, token);
  }

  @Override
  public String toString() {
    return (
      "Submission{" +
      "id=" +
      id +
      ", token='" +
      token +
      '\'' +
      ", user=" +
      (user != null ? user.getUsername() : null) +
      ", languageId=" +
      languageId +
      ", statusId=" +
      statusId +
      ", statusDescription='" +
      statusDescription +
      '\'' +
      ", createdAt=" +
      createdAt +
      ", finishedAt=" +
      finishedAt +
      '}'
    );
  }
}
