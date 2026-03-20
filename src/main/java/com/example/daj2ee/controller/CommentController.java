package com.example.daj2ee.controller;

import com.example.daj2ee.dto.request.CreateCommentRequest;
import com.example.daj2ee.dto.response.BaseResponse;
import com.example.daj2ee.dto.response.CommentDto;
import com.example.daj2ee.dto.response.CommentWithRepliesDto;
import com.example.daj2ee.security.UserPrincipal;
import com.example.daj2ee.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

  private static final Logger log = LoggerFactory.getLogger(
    CommentController.class
  );

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping("/problems/{problemId}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<CommentDto>> createComment(
    @PathVariable Long problemId,
    @Valid @RequestBody CreateCommentRequest request,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      CommentDto created = commentService.addComment(
        problemId,
        request.content(),
        request.parentId(),
        principal
      );
      return BaseResponse.createdEntity(created);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to create comment: {}", e.getMessage());
      return BaseResponse.badRequestEntity(e.getMessage());
    } catch (Exception e) {
      log.error("Error while creating comment", e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @GetMapping("/problems/{problemId}")
  public ResponseEntity<BaseResponse<List<CommentDto>>> getCommentsByProblem(
    @PathVariable Long problemId
  ) {
    try {
      List<CommentDto> comments = commentService.getCommentsByProblemId(
        problemId
      );
      return BaseResponse.okEntity(comments);
    } catch (Exception e) {
      log.error("Error fetching comments for problem {}", problemId, e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @GetMapping("/problems/{problemId}/with-replies")
  public ResponseEntity<
    BaseResponse<List<CommentWithRepliesDto>>
  > getCommentsByProblemWithReplies(@PathVariable Long problemId) {
    try {
      List<CommentWithRepliesDto> comments =
        commentService.getCommentsByProblemIdWithReplies(problemId);
      return BaseResponse.okEntity(comments);
    } catch (Exception e) {
      log.error(
        "Error fetching comments with replies for problem {}",
        problemId,
        e
      );
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @PostMapping("/{commentId}/upvote")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<CommentDto>> upvoteComment(
    @PathVariable Long commentId,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      CommentDto updated = commentService.upvoteComment(commentId, principal);
      return BaseResponse.okEntity(updated);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to upvote comment: {}", e.getMessage());
      return BaseResponse.notFoundEntity(e.getMessage());
    } catch (Exception e) {
      log.error("Error while upvoting comment", e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }

  @PostMapping("/{commentId}/downvote")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<CommentDto>> downvoteComment(
    @PathVariable Long commentId,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    try {
      CommentDto updated = commentService.downvoteComment(commentId, principal);
      return BaseResponse.okEntity(updated);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to downvote comment: {}", e.getMessage());
      return BaseResponse.notFoundEntity(e.getMessage());
    } catch (Exception e) {
      log.error("Error while downvoting comment", e);
      return BaseResponse.serverErrorEntity(e.getMessage());
    }
  }
}
