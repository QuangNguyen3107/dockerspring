package com.example.daj2ee.service;

import com.example.daj2ee.dto.response.CommentDto;
import com.example.daj2ee.dto.response.CommentWithRepliesDto;
import com.example.daj2ee.entity.Comment;
import com.example.daj2ee.entity.Problem;
import com.example.daj2ee.entity.User;
import com.example.daj2ee.repository.CommentRepository;
import com.example.daj2ee.repository.ProblemRepository;
import com.example.daj2ee.repository.UserRepository;
import com.example.daj2ee.security.UserPrincipal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ProblemRepository problemRepository;

  public CommentServiceImpl(
    CommentRepository commentRepository,
    UserRepository userRepository,
    ProblemRepository problemRepository
  ) {
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
    this.problemRepository = problemRepository;
  }

  @Override
  public CommentDto addComment(
    Long problemId,
    String content,
    Long parentId,
    UserPrincipal principal
  ) {
    User author = userRepository
      .findByUsername(principal.getUsername())
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Problem problem = problemRepository
      .findById(problemId)
      .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

    Comment parent = null;
    if (parentId != null) {
      parent = commentRepository
        .findById(parentId)
        .orElseThrow(() ->
          new IllegalArgumentException("Parent comment not found")
        );

      if (parent.getParent() != null) {
        throw new IllegalArgumentException(
          "Comments can only have maximum 2 levels. Cannot reply to a reply."
        );
      }
    }

    Comment comment = new Comment(problem, author, content, parent);
    comment.setCreatedAt(LocalDateTime.now());
    comment.setUpvotes(0);
    comment.setDownvotes(0);

    Comment saved = commentRepository.save(comment);
    return mapToDto(saved);
  }

  @Override
  public List<CommentDto> getCommentsByProblemId(Long problemId) {
    return commentRepository
      .findByProblemId(problemId)
      .stream()
      .map(this::mapToDto)
      .collect(Collectors.toList());
  }

  @Override
  public List<CommentWithRepliesDto> getCommentsByProblemIdWithReplies(
    Long problemId
  ) {
    List<Comment> topLevelComments =
      commentRepository.findTopLevelCommentsByProblemId(problemId);

    return topLevelComments
      .stream()
      .map(this::mapToDtoWithReplies)
      .collect(Collectors.toList());
  }

  @Override
  public CommentDto upvoteComment(Long commentId, UserPrincipal principal) {
    Comment comment = commentRepository
      .findById(commentId)
      .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

    comment.setUpvotes(comment.getUpvotes() + 1);
    Comment updated = commentRepository.save(comment);
    return mapToDto(updated);
  }

  @Override
  public CommentDto downvoteComment(Long commentId, UserPrincipal principal) {
    Comment comment = commentRepository
      .findById(commentId)
      .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

    comment.setDownvotes(comment.getDownvotes() + 1);
    Comment updated = commentRepository.save(comment);
    return mapToDto(updated);
  }

  private CommentDto mapToDto(Comment comment) {
    return new CommentDto(
      comment.getId(),
      comment.getProblem().getId(),
      comment.getAuthor().getUsername(),
      comment.getContent(),
      comment.getUpvotes(),
      comment.getDownvotes(),
      comment.getCreatedAt(),
      comment.getParent() != null ? comment.getParent().getId() : null
    );
  }

  private CommentWithRepliesDto mapToDtoWithReplies(Comment comment) {
    List<Comment> replies = commentRepository.findRepliesByParentId(
      comment.getId()
    );

    return new CommentWithRepliesDto(
      comment.getId(),
      comment.getProblem().getId(),
      comment.getAuthor().getUsername(),
      comment.getContent(),
      comment.getUpvotes(),
      comment.getDownvotes(),
      comment.getCreatedAt(),
      replies.stream().map(this::mapToDto).collect(Collectors.toList())
    );
  }
}
