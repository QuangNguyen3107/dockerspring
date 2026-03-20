package com.example.daj2ee.controller;

import com.example.daj2ee.dto.request.SubmissionRequest;
import com.example.daj2ee.dto.response.BaseResponse;
import com.example.daj2ee.dto.response.SubmissionResponse;
import com.example.daj2ee.security.UserPrincipal;
import com.example.daj2ee.service.SubmissionServiceImpl;
import jakarta.validation.Valid;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmissionController {

  private static final Logger log = LoggerFactory.getLogger(
    SubmissionController.class
  );

  private final SubmissionServiceImpl submissionService;

  public SubmissionController(SubmissionServiceImpl submissionService) {
    this.submissionService = submissionService;
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<BaseResponse<SubmissionResponse>> submit(
    @Valid @RequestBody SubmissionRequest request,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      String username = principal == null ? null : principal.getUsername();
      SubmissionResponse resp = submissionService.submit(request, username);

      if (Boolean.TRUE.equals(request.waitForResult())) {
        return BaseResponse.<SubmissionResponse>okEntity(resp);
      } else {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
          .path("/{token}")
          .buildAndExpand(resp.token())
          .toUri();
        return ResponseEntity.accepted()
          .location(location)
          .body(BaseResponse.<SubmissionResponse>accepted(resp));
      }
    } catch (HttpClientErrorException ex) {
      log.warn("Judge0 error: {}", ex.getMessage());
      return BaseResponse.<SubmissionResponse>build(
        ex.getStatusCode().value(),
        "Failed to create submission: " + ex.getResponseBodyAsString(),
        null
      ).toResponseEntity();
    } catch (Exception ex) {
      log.error("Error during submission", ex);
      return BaseResponse.serverErrorEntity("Internal server error");
    }
  }

  @GetMapping("/{token}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<BaseResponse<SubmissionResponse>> getByToken(
    @PathVariable String token,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      SubmissionResponse resp = submissionService.getSubmissionByToken(token);
      if (resp == null) {
        return BaseResponse.<SubmissionResponse>notFoundEntity(
          "Submission not found"
        );
      }

      String author = resp.username();
      if (author != null) {
        boolean isAuthor =
          principal.getUsername() != null &&
          principal.getUsername().equals(author);
        boolean isAdmin = principal
          .getAuthorities()
          .stream()
          .map(GrantedAuthority::getAuthority)
          .anyMatch("ROLE_ADMIN"::equals);

        if (!isAuthor && !isAdmin) {
          return BaseResponse.<SubmissionResponse>forbiddenEntity("Forbidden");
        }
      }

      return BaseResponse.<SubmissionResponse>okEntity(resp);
    } catch (HttpClientErrorException.NotFound nf) {
      return BaseResponse.<SubmissionResponse>notFoundEntity(
        "Submission not found"
      );
    } catch (HttpClientErrorException ex) {
      log.warn("Judge0 error: {}", ex.getMessage());
      return BaseResponse.<SubmissionResponse>build(
        ex.getStatusCode().value(),
        "Failed to fetch submission: " + ex.getResponseBodyAsString(),
        null
      ).toResponseEntity();
    } catch (Exception ex) {
      log.error("Error fetching submission", ex);
      return BaseResponse.serverErrorEntity("Internal server error");
    }
  }
}
