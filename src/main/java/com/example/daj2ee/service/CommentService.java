package com.example.daj2ee.service;

import com.example.daj2ee.dto.response.CommentDto;
import com.example.daj2ee.dto.response.CommentWithRepliesDto;
import com.example.daj2ee.security.UserPrincipal;
import java.util.List;

public interface CommentService {
  CommentDto addComment(
    Long problemId,
    String content,
    Long parentId,
    UserPrincipal principal
  );

  List<CommentDto> getCommentsByProblemId(Long problemId);

  List<CommentWithRepliesDto> getCommentsByProblemIdWithReplies(Long problemId);

  CommentDto upvoteComment(Long commentId, UserPrincipal principal);

  CommentDto downvoteComment(Long commentId, UserPrincipal principal);
}
