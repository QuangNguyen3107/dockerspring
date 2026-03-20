package com.example.daj2ee.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CommentWithRepliesDto(
  Long id,
  Long problemId,
  String authorUsername,
  String content,
  Integer upvotes,
  Integer downvotes,
  LocalDateTime createdAt,
  List<CommentDto> replies
) {}
