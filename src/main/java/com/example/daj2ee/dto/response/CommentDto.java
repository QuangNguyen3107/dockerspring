package com.example.daj2ee.dto.response;

import java.time.LocalDateTime;

public record CommentDto(
  Long id,
  Long problemId,
  String authorUsername,
  String content,
  Integer upvotes,
  Integer downvotes,
  LocalDateTime createdAt,
  Long parentId
) {}
