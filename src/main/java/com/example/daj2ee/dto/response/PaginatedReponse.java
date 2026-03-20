package com.example.daj2ee.dto.response;

public record PaginatedReponse<T>(
  long totalItems,
  int totalPages,
  int currentPage,
  int pageSize,
  T items
) {}
