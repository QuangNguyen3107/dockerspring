package com.example.daj2ee.service;

import com.example.daj2ee.dto.request.SubmissionRequest;
import com.example.daj2ee.dto.response.SubmissionResponse;
import com.example.daj2ee.dto.response.TestCaseResult;
import com.example.daj2ee.entity.Submission;
import com.example.daj2ee.entity.TestCase;
import com.example.daj2ee.integration.judge.Judge0Client;
import com.example.daj2ee.repository.ProblemRepository;
import com.example.daj2ee.repository.SubmissionRepository;
import com.example.daj2ee.repository.TestCaseRepository;
import com.example.daj2ee.repository.UserRepository;
import com.example.daj2ee.repository.UserSolvedProblemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that orchestrates submissions to Judge0 and persists results.
 *
 * Responsibilities:
 * - Create submissions (optionally wait for result)
 * - Poll Judge0 for results and persist them
 * - Retrieve submission results (from DB if available, otherwise from Judge0)
 */
@Service
public class SubmissionServiceImpl implements SubmissionService {

  private static final Logger log = LoggerFactory.getLogger(
    SubmissionServiceImpl.class
  );

  private final SubmissionRepository submissionRepository;
  private final ProblemRepository problemRepository;
  private final TestCaseRepository testCaseRepository;
  private final Judge0Client judge0Client;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;
  private final UserSolvedProblemRepository userSolvedProblemRepository;

  private final long pollIntervalMs;
  private final long waitTimeoutMs;

  public SubmissionServiceImpl(
    SubmissionRepository submissionRepository,
    ProblemRepository problemRepository,
    TestCaseRepository testCaseRepository,
    Judge0Client judge0Client,
    UserRepository userRepository,
    ObjectMapper objectMapper,
    UserSolvedProblemRepository userSolvedProblemRepository,
    @Value("${app.judge0.poll-interval-ms:500}") long pollIntervalMs,
    @Value("${app.judge0.wait-timeout-ms:15000}") long waitTimeoutMs
  ) {
    this.submissionRepository = submissionRepository;
    this.problemRepository = problemRepository;
    this.testCaseRepository = testCaseRepository;
    this.judge0Client = judge0Client;
    this.userRepository = userRepository;
    this.objectMapper = objectMapper;
    this.userSolvedProblemRepository = userSolvedProblemRepository;
    this.pollIntervalMs = pollIntervalMs;
    this.waitTimeoutMs = waitTimeoutMs;
  }

  /**
   * Submit code to Judge0 and optionally wait for result.
   *
   * When the request includes a {@code problemId}, the code is automatically run against all
   * of the problem's test cases using Judge0's batch API. Per-test-case verdicts are returned
   * in {@link SubmissionResponse#getTestCaseResults()}.
   *
   * When no {@code problemId} is given (or the problem has no test cases), the code is submitted
   * as a plain single run using whatever {@code stdin}/{@code expectedOutput} the client provided.
   *
   * @param request  submission request (languageId, sourceCode, problemId, etc.)
   * @param username username of submitter (may be null for anonymous)
   * @return SubmissionResponse containing token and, if requested, the final result + test verdicts
   */
  @Transactional
  public SubmissionResponse submit(SubmissionRequest request, String username) {
    // Create initial submission record
    Submission submission = new Submission();
    submission.setSourceCode(request.sourceCode());
    submission.setLanguageId(request.languageId());
    submission.setStdin(request.stdin());

    if (request.problemId() != null) {
      problemRepository
        .findById(request.problemId())
        .ifPresent(submission::setProblem);
    }

    if (username != null && !username.isBlank()) {
      userRepository.findByUsername(username).ifPresent(submission::setUser);
    }

    submission = submissionRepository.save(submission);

    boolean base64 = Boolean.TRUE.equals(request.base64Encoded());
    boolean waitForResult = Boolean.TRUE.equals(request.waitForResult());

    // ── Test-case path: run against all problem test cases via batch API ──────
    if (request.problemId() != null) {
      List<TestCase> testCases =
        testCaseRepository.findByProblemIdOrderBySortOrderAsc(
          request.problemId()
        );

      if (!testCases.isEmpty()) {
        return submitWithTestCases(
          submission,
          request,
          testCases,
          base64,
          waitForResult
        );
      }
    }

    // ── Plain single-run path ─────────────────────────────────────────────────
    Map<String, Object> body = buildRequestBody(request);

    Map<String, Object> createResp = judge0Client.createSubmission(
      body,
      base64
    );
    Object tokenObj = createResp.get("token");
    if (!(tokenObj instanceof String)) {
      saveRawResponseOnSubmission(submission, createResp);
      return SubmissionResponse.fromEntity(submission);
    }

    String token = (String) tokenObj;
    submission.setToken(token);
    submissionRepository.save(submission);

    if (!waitForResult) {
      return new SubmissionResponse(
        submission.getId(),
        token,
        null,
        submission.getLanguageId(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
      );
    }

    // Synchronous mode: poll until finished or timeout
    Instant start = Instant.now();
    while (true) {
      Map<String, Object> result = judge0Client.getSubmission(
        token,
        base64,
        "*"
      );
      applyJudge0ResultToSubmission(submission, result);
      submissionRepository.save(submission);

      if (isFinished(result)) {
        return SubmissionResponse.fromEntity(submission);
      }

      if (Duration.between(start, Instant.now()).toMillis() >= waitTimeoutMs) {
        log.debug(
          "Timeout waiting for Judge0 submission token={} after {}ms",
          token,
          waitTimeoutMs
        );
        return SubmissionResponse.fromEntity(submission);
      }

      try {
        Thread.sleep(Math.max(50, pollIntervalMs));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(
          "Interrupted while waiting for submission result",
          e
        );
      }
    }
  }

  /**
   * Run the submitted code against every test case using Judge0's batch API.
   *
   * Flow:
   *  1. POST /submissions/batch — one entry per test case
   *  2. Poll GET /submissions/batch?tokens=… until all are finished or timeout
   *  3. Build per-test-case {@link TestCaseResult} objects by comparing stdout to expectedOutput
   *  4. Set the first test case's token on the Submission entity for traceability
   *
   * @param submission    the already-persisted Submission entity
   * @param request       the original submission request
   * @param testCases     all test cases belonging to the problem, in order
   * @param base64        whether to use base64 encoding
   * @param waitForResult if false, fire-and-forget (tokens returned but no verdicts yet)
   */
  private SubmissionResponse submitWithTestCases(
    Submission submission,
    SubmissionRequest request,
    List<TestCase> testCases,
    boolean base64,
    boolean waitForResult
  ) {
    if (request.stdin() != null || request.expectedOutput() != null) {
      log.warn(
        "Submission for problemId={} includes stdin/expectedOutput in the request body — " +
          "these fields are ignored when a problemId is provided; test case values are used instead.",
        request.problemId()
      );
    }

    // Build one Judge0 request body per test case, using only base fields (code + limits).
    // stdin and expected_output come exclusively from each test case.
    List<Map<String, Object>> batchBodies = testCases
      .stream()
      .map(tc -> {
        Map<String, Object> body = buildBaseRequestBody(request);
        body.put("stdin", tc.getInput() != null ? tc.getInput() : "");
        body.put("expected_output", tc.getExpectedOutput());
        return body;
      })
      .collect(Collectors.toList());

    // Submit all at once
    List<Map<String, Object>> batchResp = judge0Client.createBatchSubmissions(
      batchBodies,
      base64
    );

    List<String> tokens = batchResp
      .stream()
      .map(r -> r.get("token"))
      .filter(t -> t instanceof String)
      .map(t -> (String) t)
      .collect(Collectors.toList());

    // Persist the first token on the submission for traceability
    if (!tokens.isEmpty()) {
      submission.setToken(tokens.get(0));
      submissionRepository.save(submission);
    }

    if (!waitForResult || tokens.isEmpty()) {
      SubmissionResponse resp = SubmissionResponse.fromEntity(submission);
      resp = new SubmissionResponse(
        resp.id(),
        tokens.isEmpty() ? null : tokens.get(0),
        resp.problemId(),
        resp.languageId(),
        resp.statusId(),
        resp.statusDescription(),
        resp.stdout(),
        resp.stderr(),
        resp.compileOutput(),
        resp.message(),
        resp.time(),
        resp.memory(),
        resp.username(),
        resp.testCaseResults(),
        resp.allPassed(),
        resp.firstSolve(),
        resp.createdAt(),
        resp.finishedAt()
      );
      return resp;
    }

    // Poll until all test cases are finished
    List<Map<String, Object>> results = pollBatchUntilDone(tokens, base64);

    // Build per-test-case verdicts
    List<TestCaseResult> testCaseResults = new ArrayList<>();
    for (int i = 0; i < testCases.size(); i++) {
      TestCase tc = testCases.get(i);
      Map<String, Object> res = i < results.size() ? results.get(i) : Map.of();
      testCaseResults.add(buildTestCaseResult(i + 1, tc, res));
    }

    boolean allPassed = testCaseResults
      .stream()
      .allMatch(TestCaseResult::passed);

    // Reflect the overall verdict back onto the submission entity
    // Use the first result's status as the representative status
    if (!results.isEmpty()) {
      applyJudge0ResultToSubmission(submission, results.get(0));
    }
    submissionRepository.save(submission);

    boolean firstSolve = false;
    if (
      allPassed &&
      submission.getUser() != null &&
      submission.getProblem() != null
    ) {
      Long userId = submission.getUser().getId();
      Long problemId = submission.getProblem().getId();
      if (
        !userSolvedProblemRepository.existsByUserIdAndProblemId(
          userId,
          problemId
        )
      ) {
        userSolvedProblemRepository.save(
          new com.example.daj2ee.entity.UserSolvedProblem(
            submission.getUser(),
            submission.getProblem(),
            submission.getLanguageId()
          )
        );
        firstSolve = true;
        log.debug(
          "Marked problem id={} as solved for user id={}",
          problemId,
          userId
        );
      }
    }

    SubmissionResponse respEntity = SubmissionResponse.fromEntity(submission);
    return new SubmissionResponse(
      respEntity.id(),
      respEntity.token(),
      respEntity.problemId(),
      respEntity.languageId(),
      respEntity.statusId(),
      respEntity.statusDescription(),
      respEntity.stdout(),
      respEntity.stderr(),
      respEntity.compileOutput(),
      respEntity.message(),
      respEntity.time(),
      respEntity.memory(),
      respEntity.username(),
      testCaseResults,
      allPassed,
      allPassed ? firstSolve : null,
      respEntity.createdAt(),
      respEntity.finishedAt()
    );
  }

  /**
   * Poll Judge0's batch endpoint repeatedly until every token reports a finished status,
   * or until {@link #waitTimeoutMs} elapses.
   */
  private List<Map<String, Object>> pollBatchUntilDone(
    List<String> tokens,
    boolean base64
  ) {
    Instant start = Instant.now();
    List<Map<String, Object>> results = List.of();

    while (true) {
      results = judge0Client.getBatchSubmissions(tokens, base64);

      boolean allDone = results.stream().allMatch(this::isFinished);
      if (allDone) break;

      if (Duration.between(start, Instant.now()).toMillis() >= waitTimeoutMs) {
        log.debug(
          "Batch poll timeout after {}ms for {} tokens",
          waitTimeoutMs,
          tokens.size()
        );
        break;
      }

      try {
        Thread.sleep(Math.max(50, pollIntervalMs));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(
          "Interrupted while waiting for batch submission results",
          e
        );
      }
    }

    return results;
  }

  /**
   * Build a {@link TestCaseResult} from a Judge0 result map and the original test case.
   * A test case is considered passed when the Judge0 status is "Accepted" (id=3) OR
   * when stdout matches expectedOutput exactly (trimming trailing newline).
   */
  @SuppressWarnings("unchecked")
  private TestCaseResult buildTestCaseResult(
    int index,
    TestCase testCase,
    Map<String, Object> result
  ) {
    boolean hidden = testCase.isHidden();
    String expectedOutput = testCase.getExpectedOutput();

    Object stdout = result.get("stdout");
    String stdoutStr = stdout != null ? String.valueOf(stdout) : null;

    Object stderr = result.get("stderr");
    String stderrStr = stderr != null ? String.valueOf(stderr) : null;

    Object compile = result.get("compile_output");
    String compileStr = compile != null ? String.valueOf(compile) : null;

    Object time = result.get("time");
    String timeStr = time != null ? String.valueOf(time) : null;

    Object memory = result.get("memory");
    Long memoryVal =
      memory instanceof Number ? ((Number) memory).longValue() : null;

    Integer statusId = extractStatusId(result);
    String statusDesc = extractStatusDescription(result);

    // Judge0 status 3 = Accepted (stdout matches expected_output on Judge0's side)
    boolean passed = false;
    if (statusId != null && statusId == 3) {
      passed = true;
    } else if (stdoutStr != null && expectedOutput != null) {
      // Fallback: normalize trailing whitespace and compare locally
      passed = normalize(stdoutStr).equals(normalize(expectedOutput));
    }

    return new TestCaseResult(
      index,
      passed,
      hidden,
      stdoutStr,
      stderrStr,
      compileStr,
      expectedOutput,
      statusId,
      statusDesc,
      timeStr,
      memoryVal
    );
  }

  /** Trim trailing whitespace/newlines for lenient output comparison. */
  private String normalize(String s) {
    return s == null ? "" : s.stripTrailing();
  }

  /**
   * Retrieve a submission result by token. If the submission exists in DB and is finished, return it.
   * Otherwise query Judge0 and persist the retrieved result if possible.
   *
   * @param token submission token
   * @return SubmissionResponse with current result/status
   */
  @Transactional
  public SubmissionResponse getSubmissionByToken(String token) {
    Optional<Submission> opt = submissionRepository.findByToken(token);
    boolean base64 = false; // do not request base64 by default — client can specify later (not implemented)
    if (opt.isPresent()) {
      Submission s = opt.get();
      if (s.isFinished()) {
        return SubmissionResponse.fromEntity(s);
      }
      // Not finished - fetch current status from Judge0
      Map<String, Object> result = judge0Client.getSubmission(
        token,
        base64,
        "*"
      );
      applyJudge0ResultToSubmission(s, result);
      submissionRepository.save(s);
      return SubmissionResponse.fromEntity(s);
    } else {
      // No local record - fetch from Judge0 and optionally persist
      Map<String, Object> result = judge0Client.getSubmission(
        token,
        base64,
        "*"
      );
      // Only create a local record once the result is finished; if Judge0 still
      // reports In Queue / Processing there is no point persisting an incomplete
      // row that will immediately re-appear in the poller's pending query.
      Integer statusId = extractStatusId(result);
      boolean finished = statusId != null && statusId > 2;
      if (!finished) {
        log.debug(
          "Skipping phantom persistence for token={} — still pending (statusId={})",
          token,
          statusId
        );
        Submission transient_ = new Submission();
        transient_.setToken(token);
        applyJudge0ResultToSubmission(transient_, result);
        return SubmissionResponse.fromEntity(transient_);
      }
      // Persist a new submission entry based on the finished result
      Submission s = new Submission();
      s.setToken(token);
      // try to extract some basic fields
      Object langId = result.get("language_id");
      if (langId instanceof Number) s.setLanguageId(
        ((Number) langId).intValue()
      );
      applyJudge0ResultToSubmission(s, result);
      s = submissionRepository.save(s);
      return SubmissionResponse.fromEntity(s);
    }
  }

  // -------------------------
  // Helper methods
  // -------------------------

  @SuppressWarnings("unchecked")
  private void applyJudge0ResultToSubmission(
    Submission submission,
    Map<String, Object> result
  ) {
    if (result == null) return;

    // Only persist the raw JSON blob when the submission is finished to avoid
    // accumulating large strings in the JPA session cache on every poll tick.
    Integer statusId = extractStatusId(result);
    boolean finished = statusId != null && statusId > 2;
    if (finished) {
      try {
        submission.setRawResponse(objectMapper.writeValueAsString(result));
      } catch (Exception ex) {
        // ignore serialization errors for rawResponse
        log.debug("Failed to serialize Judge0 raw response", ex);
      }
    }

    // stdout / stderr / compile_output
    Object stdout = result.get("stdout");
    if (stdout != null) submission.setStdout(String.valueOf(stdout));

    Object stderr = result.get("stderr");
    if (stderr != null) submission.setStderr(String.valueOf(stderr));

    Object compile = result.get("compile_output");
    if (compile != null) submission.setCompileOutput(String.valueOf(compile));

    // message (e.g. exit code info)
    Object message = result.get("message");
    if (message != null) submission.setMessage(String.valueOf(message));

    // time and memory
    Object time = result.get("time");
    if (time != null) submission.setTime(String.valueOf(time));

    Object memory = result.get("memory");
    if (memory instanceof Number) submission.setMemory(
      ((Number) memory).longValue()
    );
    else if (memory != null) {
      try {
        submission.setMemory(Long.parseLong(String.valueOf(memory)));
      } catch (NumberFormatException ignored) {}
    }

    // status (statusId was already extracted above for the rawResponse guard)
    if (statusId != null) submission.setStatusId(statusId);

    String statusDescription = extractStatusDescription(result);
    if (statusDescription != null) submission.setStatusDescription(
      statusDescription
    );

    // If finished, set finishedAt
    if (submission.getFinishedAt() == null && submission.isFinished()) {
      submission.setFinishedAt(LocalDateTime.now());
    }
  }

  private void saveRawResponseOnSubmission(
    Submission submission,
    Map<String, Object> raw
  ) {
    try {
      submission.setRawResponse(objectMapper.writeValueAsString(raw));
      submissionRepository.save(submission);
    } catch (Exception ex) {
      log.debug("Failed to save raw response on submission", ex);
    }
  }

  @SuppressWarnings("unchecked")
  private Integer extractStatusId(Map<String, Object> result) {
    if (result == null) return null;
    Object statusObj = result.get("status");
    if (statusObj instanceof Map) {
      Object id = ((Map<String, Object>) statusObj).get("id");
      if (id instanceof Number) return ((Number) id).intValue();
      if (id instanceof String) {
        try {
          return Integer.parseInt((String) id);
        } catch (NumberFormatException ignored) {}
      }
    }
    // fallback to top-level status_id
    Object sid = result.get("status_id");
    if (sid instanceof Number) return ((Number) sid).intValue();
    if (sid instanceof String) {
      try {
        return Integer.parseInt((String) sid);
      } catch (NumberFormatException ignored) {}
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private String extractStatusDescription(Map<String, Object> result) {
    if (result == null) return null;
    Object statusObj = result.get("status");
    if (statusObj instanceof Map) {
      Object desc = ((Map<String, Object>) statusObj).get("description");
      if (desc != null) return String.valueOf(desc);
    }
    Object descTop = result.get("status_description");
    if (descTop != null) return String.valueOf(descTop);
    return null;
  }

  private boolean isFinished(Map<String, Object> result) {
    Integer sid = extractStatusId(result);
    return sid != null && sid > 2;
  }

  /**
   * Builds the Judge0 request body for a plain (non-test-case) submission.
   * Includes source_code, language_id, stdin, expected_output, and all execution limits
   * present on the request.
   */
  private Map<String, Object> buildRequestBody(SubmissionRequest request) {
    Map<String, Object> body = buildBaseRequestBody(request);
    if (request.stdin() != null) body.put("stdin", request.stdin());
    if (request.expectedOutput() != null) body.put(
      "expected_output",
      request.expectedOutput()
    );
    return body;
  }

  /**
   * Builds the base Judge0 request body containing only source_code, language_id,
   * and execution-limit fields. stdin and expected_output are intentionally excluded
   * so that callers (e.g. the batch test-case path) can set them explicitly per test case.
   */
  private Map<String, Object> buildBaseRequestBody(SubmissionRequest request) {
    Map<String, Object> body = new java.util.HashMap<>();
    body.put("language_id", request.languageId());
    body.put("source_code", request.sourceCode());
    if (request.numberOfRuns() != null) body.put(
      "number_of_runs",
      request.numberOfRuns()
    );
    if (request.cpuTimeLimit() != null) body.put(
      "cpu_time_limit",
      request.cpuTimeLimit()
    );
    if (request.cpuExtraTime() != null) body.put(
      "cpu_extra_time",
      request.cpuExtraTime()
    );
    if (request.wallTimeLimit() != null) body.put(
      "wall_time_limit",
      request.wallTimeLimit()
    );
    if (request.memoryLimit() != null) body.put(
      "memory_limit",
      request.memoryLimit()
    );
    if (request.maxFileSize() != null) body.put(
      "max_file_size",
      request.maxFileSize()
    );
    return body;
  }
}
