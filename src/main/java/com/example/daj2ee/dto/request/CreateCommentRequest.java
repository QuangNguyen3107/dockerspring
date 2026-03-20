package com.example.daj2ee.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
  @NotBlank(message = "Content cannot be blank") String content,
  Long parentId
) {}
