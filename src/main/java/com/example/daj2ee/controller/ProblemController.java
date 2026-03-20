package com.example.daj2ee.controller;

import com.example.daj2ee.dto.response.BaseResponse;
import com.example.daj2ee.dto.response.PaginatedReponse;
import com.example.daj2ee.dto.response.ProblemDto;
import com.example.daj2ee.dto.response.ProblemSummaryDto;
import com.example.daj2ee.security.UserPrincipal;
import com.example.daj2ee.service.ProblemService;
import com.example.daj2ee.util.shared.PaginationConstants;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
@CrossOrigin(origins = "*")
public class ProblemController {

  private static final Logger log = LoggerFactory.getLogger(
    ProblemController.class
  );

  private final ProblemService problemService;

  public ProblemController(ProblemService problemService) {
    this.problemService = problemService;
  }

  @GetMapping
  public ResponseEntity<
    BaseResponse<PaginatedReponse<List<ProblemSummaryDto>>>
  > listPublished(
    @RequestParam(
      defaultValue = "" + PaginationConstants.DEFAULT_PAGE
    ) int page,
    @RequestParam(
      defaultValue = "" + PaginationConstants.DEFAULT_PAGE_SIZE
    ) int size,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      Long userId = principal != null ? principal.getId() : null;
      PaginatedReponse<List<ProblemSummaryDto>> response =
        problemService.findPublished(page, size, userId);
      return BaseResponse.okEntity(response);
    } catch (Exception e) {
      log.error("Error listing published problems", e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseResponse<ProblemDto>> getById(
    @PathVariable Long id,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      Long userId = principal != null ? principal.getId() : null;
      ProblemDto problem = problemService.getById(id, userId);
      if (problem == null) {
        return BaseResponse.<ProblemDto>notFoundEntity("Problem not found");
      }
      return BaseResponse.okEntity(problem);
    } catch (Exception e) {
      log.error("Error getting problem with id: {}", id, e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @PostMapping
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<ProblemDto>> create(
    @Valid @RequestBody ProblemDto problemDto,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      ProblemDto created = problemService.create(
        problemDto,
        principal.getUsername()
      );
      return BaseResponse.createdEntity(created);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid problem data: {}", e.getMessage());
      return BaseResponse.badRequestEntity(e.getMessage());
    } catch (Exception e) {
      log.error("Error creating problem", e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<ProblemDto>> update(
    @PathVariable Long id,
    @Valid @RequestBody ProblemDto problemDto,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      ProblemDto updated = problemService.update(
        id,
        problemDto,
        principal.getUsername()
      );
      return BaseResponse.okEntity(updated);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not found")) {
        return BaseResponse.<ProblemDto>notFoundEntity(e.getMessage());
      }
      if (e.getMessage().contains("authorized")) {
        return BaseResponse.<ProblemDto>forbiddenEntity(e.getMessage());
      }
      log.error("Error updating problem {}", id, e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<Void>> delete(
    @PathVariable Long id,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      problemService.delete(id, principal.getUsername());
      return BaseResponse.<Void>okEntity(null);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not found")) {
        return BaseResponse.<Void>notFoundEntity(e.getMessage());
      }
      if (e.getMessage().contains("authorized")) {
        return BaseResponse.<Void>forbiddenEntity(e.getMessage());
      }
      log.error("Error deleting problem {}", id, e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @GetMapping("/difficulty/{level}")
  public ResponseEntity<BaseResponse<List<ProblemSummaryDto>>> listByDifficulty(
    @PathVariable String level,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      Long userId = principal != null ? principal.getId() : null;
      List<ProblemSummaryDto> problems = problemService.findByDifficulty(
        level.toUpperCase(),
        userId
      );
      return BaseResponse.okEntity(problems);
    } catch (Exception e) {
      log.error("Error listing problems by difficulty: {}", level, e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }
}
