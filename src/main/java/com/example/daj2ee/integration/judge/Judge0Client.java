package com.example.daj2ee.integration.judge;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Lightweight client for Judge0 REST API.
 *
 * Features:
 * - createSubmission: creates a submission and returns the submission response (contains token)
 * - getSubmission: fetches submission result by token
 * - createSubmissionAndWait: convenience: create + poll until finished or timeout
 *
 * Configurable via properties:
 * - app.judge0.url (default: https://ce.judge0.com)
 * - app.judge0.apiKey (optional)
 */
@Component
public class Judge0Client {

  private static final Logger log = LoggerFactory.getLogger(Judge0Client.class);

  private final RestTemplate restTemplate = new RestTemplate();
  private final String judge0BaseUrl;
  private final String apiKey;

  public Judge0Client(
    @Value("${app.judge0.url:https://ce.judge0.com}") String judge0Url,
    @Value("${app.judge0.apiKey:}") String apiKey
  ) {
    // normalize base URL (no trailing slash)
    if (judge0Url.endsWith("/")) {
      this.judge0BaseUrl = judge0Url.substring(0, judge0Url.length() - 1);
    } else {
      this.judge0BaseUrl = judge0Url;
    }
    this.apiKey = (apiKey == null || apiKey.isBlank()) ? null : apiKey;
  }

  /**
   * Create a submission on Judge0. Returns the JSON response as a map. Usually contains a {@code token} key.
   *
   * @param requestBody    map containing submission fields (source_code, language_id, stdin, ...)
   * @param base64Encoded  whether the body strings are Base64 encoded
   * @return map response from Judge0 (may contain token or full result)
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> createSubmission(
    Map<String, Object> requestBody,
    boolean base64Encoded
  ) {
    URI uri = UriComponentsBuilder.fromUriString(judge0BaseUrl)
      .pathSegment("submissions")
      .queryParam("base64_encoded", base64Encoded)
      .build()
      .toUri();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (apiKey != null) {
      headers.set("X-Auth-Token", apiKey);
    }

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
      requestBody,
      headers
    );
    try {
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        uri,
        HttpMethod.POST,
        entity,
        new ParameterizedTypeReference<Map<String, Object>>() {}
      );
      if (
        response.getStatusCode().is2xxSuccessful() ||
        response.getStatusCode() == HttpStatus.CREATED
      ) {
        Map<String, Object> body = response.getBody();
        return body == null ? new HashMap<>() : body;
      } else {
        log.warn(
          "Unexpected response from Judge0 create submission: {}",
          response.getStatusCode()
        );
        return Map.of();
      }
    } catch (HttpClientErrorException ex) {
      log.warn(
        "Judge0 create submission failed: status={}, body={}",
        ex.getStatusCode(),
        ex.getResponseBodyAsString()
      );
      throw ex;
    }
  }

  /**
   * Retrieve submission result from Judge0 by token.
   *
   * @param token          the Judge0 submission token
   * @param base64Encoded  whether stdout/stderr/compile_output should be fetched base64 encoded
   * @param fields         optional comma-separated fields to request (use '*' for all)
   * @return response map from Judge0 (may contain stdout, stderr, status object, etc.)
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getSubmission(
    String token,
    boolean base64Encoded,
    String fields
  ) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
      judge0BaseUrl
    ).pathSegment("submissions", token);

    if (base64Encoded) builder.queryParam("base64_encoded", true);
    if (fields != null && !fields.isBlank()) builder.queryParam(
      "fields",
      fields
    );

    URI uri = builder.build().toUri();

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(MediaType.parseMediaTypes("application/json"));
    if (apiKey != null) {
      headers.set("X-Auth-Token", apiKey);
    }

    HttpEntity<Void> entity = new HttpEntity<>(headers);
    try {
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        uri,
        HttpMethod.GET,
        entity,
        new ParameterizedTypeReference<Map<String, Object>>() {}
      );
      if (response.getStatusCode().is2xxSuccessful()) {
        Map<String, Object> body = response.getBody();
        return body == null ? new HashMap<>() : body;
      } else {
        log.warn(
          "Unexpected response from Judge0 get submission: {}",
          response.getStatusCode()
        );
        return Map.of();
      }
    } catch (HttpClientErrorException ex) {
      log.warn(
        "Judge0 get submission failed: status={}, body={}",
        ex.getStatusCode(),
        ex.getResponseBodyAsString()
      );
      throw ex;
    }
  }

  /**
   * Convenience method that creates a submission and polls Judge0 until the submission finishes or the timeout elapses.
   * This method blocks until the result is available or the timeout is reached.
   *
   * @param requestBody     submission request body
   * @param base64Encoded   whether the submitted strings are base64 encoded
   * @param pollIntervalMs  poll interval in milliseconds
   * @param timeoutMs       maximum time to wait in milliseconds
   * @return a map containing the final result (GET /submissions/{token} response) or a partial result if timed out
   * @throws InterruptedException if thread is interrupted while waiting
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> createSubmissionAndWait(
    Map<String, Object> requestBody,
    boolean base64Encoded,
    long pollIntervalMs,
    long timeoutMs
  ) throws InterruptedException {
    Map<String, Object> createResponse = createSubmission(
      requestBody,
      base64Encoded
    );

    // The create response usually contains a token
    Object tokenObj = createResponse.get("token");
    if (!(tokenObj instanceof String)) {
      if (isFinished(createResponse)) {
        return createResponse;
      }
      return createResponse;
    }

    String token = (String) tokenObj;
    log.debug("Created Judge0 submission token={}", token);

    Instant start = Instant.now();
    while (true) {
      Map<String, Object> result = getSubmission(token, base64Encoded, "*");
      if (isFinished(result)) {
        return result;
      }

      // Check timeout
      if (Duration.between(start, Instant.now()).toMillis() >= timeoutMs) {
        log.debug(
          "Timeout waiting for Judge0 submission token={} after {}ms",
          token,
          timeoutMs
        );
        return result;
      }

      Thread.sleep(Math.max(50, pollIntervalMs));
    }
  }

  /**
   * Submit multiple test cases to Judge0 in a single batch request.
   *
   * Judge0's POST /submissions/batch accepts a {@code submissions} array and returns a list of
   * objects each containing a {@code token}. The tokens can then be polled via
   * {@link #getBatchSubmissions(List, boolean)}.
   *
   * @param submissions   list of individual submission request bodies
   * @param base64Encoded whether the body strings are Base64 encoded
   * @return list of maps, each containing at least a {@code token} key
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> createBatchSubmissions(
    List<Map<String, Object>> submissions,
    boolean base64Encoded
  ) {
    URI uri = UriComponentsBuilder.fromUriString(judge0BaseUrl)
      .pathSegment("submissions", "batch")
      .queryParam("base64_encoded", base64Encoded)
      .build()
      .toUri();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (apiKey != null) {
      headers.set("X-Auth-Token", apiKey);
    }

    Map<String, Object> body = new HashMap<>();
    body.put("submissions", submissions);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
    try {
      // Judge0 POST /submissions/batch returns a JSON array at the root, not a map
      ResponseEntity<List<Map<String, Object>>> response =
        restTemplate.exchange(
          uri,
          HttpMethod.POST,
          entity,
          new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
      if (
        response.getStatusCode().is2xxSuccessful() && response.getBody() != null
      ) {
        return response.getBody();
      }
      return List.of();
    } catch (HttpClientErrorException ex) {
      log.warn(
        "Judge0 batch create failed: status={}, body={}",
        ex.getStatusCode(),
        ex.getResponseBodyAsString()
      );
      throw ex;
    }
  }

  /**
   * Poll multiple submissions by their tokens in a single GET /submissions/batch request.
   *
   * @param tokens        list of Judge0 submission tokens
   * @param base64Encoded whether to request base64-encoded output fields
   * @return list of result maps in the same order as the tokens
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getBatchSubmissions(
    List<String> tokens,
    boolean base64Encoded
  ) {
    String joined = String.join(",", tokens);
    URI uri = UriComponentsBuilder.fromUriString(judge0BaseUrl)
      .pathSegment("submissions", "batch")
      .queryParam("tokens", joined)
      .queryParam("base64_encoded", base64Encoded)
      .queryParam("fields", "*")
      .build()
      .toUri();

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(MediaType.parseMediaTypes("application/json"));
    if (apiKey != null) {
      headers.set("X-Auth-Token", apiKey);
    }

    HttpEntity<Void> entity = new HttpEntity<>(headers);
    try {
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        uri,
        HttpMethod.GET,
        entity,
        new ParameterizedTypeReference<Map<String, Object>>() {}
      );
      if (
        response.getStatusCode().is2xxSuccessful() && response.getBody() != null
      ) {
        Object submissions = response.getBody().get("submissions");
        if (submissions instanceof List) {
          return (List<Map<String, Object>>) submissions;
        }
      }
      return List.of();
    } catch (HttpClientErrorException ex) {
      log.warn(
        "Judge0 batch get failed: status={}, body={}",
        ex.getStatusCode(),
        ex.getResponseBodyAsString()
      );
      throw ex;
    }
  }

  /**
   * Helper to detect whether the submission result indicates a finished state (Judge0 semantics).
   * Judge0 uses status.id with values: 1 (In Queue), 2 (Processing), >2 finished states.
   *
   * @param submissionResponse a map returned from Judge0
   * @return true if finished
   */
  @SuppressWarnings("unchecked")
  private boolean isFinished(Map<String, Object> submissionResponse) {
    if (submissionResponse == null) return false;

    // Check nested 'status' object first (common case)
    Object statusObj = submissionResponse.get("status");
    Integer statusId = null;
    if (statusObj instanceof Map) {
      Object id = ((Map<String, Object>) statusObj).get("id");
      if (id instanceof Number) statusId = ((Number) id).intValue();
      else if (id instanceof String) {
        try {
          statusId = Integer.parseInt((String) id);
        } catch (NumberFormatException ignored) {}
      }
    }

    // Fallback: some responses provide 'status_id' top-level
    if (statusId == null) {
      Object sid = submissionResponse.get("status_id");
      if (sid instanceof Number) statusId = ((Number) sid).intValue();
      else if (sid instanceof String) {
        try {
          statusId = Integer.parseInt((String) sid);
        } catch (NumberFormatException ignored) {}
      }
    }

    return statusId != null && statusId > 2;
  }
}
