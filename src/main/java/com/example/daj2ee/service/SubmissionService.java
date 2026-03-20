package com.example.daj2ee.service;

import com.example.daj2ee.dto.request.SubmissionRequest;
import com.example.daj2ee.dto.response.SubmissionResponse;

/**
 * Contract for submission-related business operations.
 *
 * Responsibilities:
 * - submit code (synchronous or asynchronous)
 * - poll/retrieve submission results by token
 */
public interface SubmissionService {

  /**
   * Submit code to Judge0 and optionally wait for result.
   *
   * @param request  submission payload (languageId, sourceCode, stdin, etc.)
   * @param username username of submitter (may be null for anonymous)
   * @return SubmissionResponse containing token and, if requested, the final result
   */
  SubmissionResponse submit(SubmissionRequest request, String username);

  /**
   * Retrieve a submission result by token. If the submission exists in DB and is finished, return it.
   * Otherwise query Judge0 and persist the retrieved result if possible.
   *
   * @param token submission token
   * @return SubmissionResponse with current result/status
   */
  SubmissionResponse getSubmissionByToken(String token);
}
