package com.example.daj2ee.service;

import com.example.daj2ee.entity.Submission;
import com.example.daj2ee.repository.SubmissionRepository;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically polls pending submissions (status 1 = In Queue, 2 = Processing)
 * and refreshes their status/result from Judge0 via {@link SubmissionServiceImpl#getSubmissionByToken(String)}.
 *
 * Notes:
 * - This is a simple in-process poller intended for small-scale/self-hosted setups (e.g., a Railway-hosted Judge0).
 * - For larger scale, consider moving to an external worker queue (Redis/RabbitMQ) or webhook-based notifications.
 */
@Component
public class SubmissionPoller {

  private static final Logger log = LoggerFactory.getLogger(
    SubmissionPoller.class
  );

  private final SubmissionRepository submissionRepository;
  private final SubmissionServiceImpl submissionService;
  private final int maxPerRun;

  public SubmissionPoller(
    SubmissionRepository submissionRepository,
    SubmissionServiceImpl submissionService,
    @Value("${app.judge0.poll-scheduler-max-per-run:20}") int maxPerRun
  ) {
    this.submissionRepository = submissionRepository;
    this.submissionService = submissionService;
    this.maxPerRun = Math.max(1, maxPerRun);
  }

  /**
   * Poll for pending submissions and refresh their status.
   *
   * Runs repeatedly with a fixed delay configured by {@code app.judge0.poll-scheduler-ms} (default 2000ms).
   */
  @Scheduled(
    fixedDelayString = "${app.judge0.poll-scheduler-ms:2000}",
    initialDelayString = "${app.judge0.poll-scheduler-ms:2000}"
  )
  public void pollPendingSubmissions() {
    try {
      List<Submission> pending = submissionRepository.findByStatusIdIn(
        Arrays.asList(1, 2),
        PageRequest.of(0, maxPerRun)
      );
      if (pending.isEmpty()) {
        log.trace("No pending submissions found to poll.");
        return;
      }

      int processed = 0;
      for (Submission submission : pending) {
        if (submission.getToken() == null || submission.getToken().isBlank()) {
          log.debug(
            "Skipping submission id={} without a Judge0 token",
            submission.getId()
          );
          continue;
        }

        try {
          // This method fetches the latest result from Judge0 and persists it
          submissionService.getSubmissionByToken(submission.getToken());
          log.debug("Polled submission token={}", submission.getToken());
        } catch (Exception ex) {
          log.warn(
            "Failed to poll submission token={}: {}",
            submission.getToken(),
            ex.getMessage()
          );
        }

        processed++;
        if (processed >= maxPerRun) {
          log.trace(
            "Processed {} submissions this run (maxPerRun={})",
            processed,
            maxPerRun
          );
          break;
        }
      }
    } catch (Exception ex) {
      log.error("Unexpected error while polling submissions", ex);
    }
  }
}
