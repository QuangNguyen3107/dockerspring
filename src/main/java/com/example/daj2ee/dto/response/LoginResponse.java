package com.example.daj2ee.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
  String accessToken,
  String tokenType,
  String username,
  List<String> roles,
  long expiresIn
) {
  public LoginResponse(
    String accessToken,
    String username,
    List<String> roles,
    long expiresIn
  ) {
    this(accessToken, "Bearer", username, roles, expiresIn);
  }
}
