package com.example.daj2ee.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
  @Size(max = 120, message = "Full name must be at most 120 characters")
  String fullName,
  @Size(max = 20, message = "Phone number must be at most 20 characters")
  @Pattern(
    regexp = "^[0-9+()\\-\\s]*$",
    message = "Phone number contains invalid characters"
  )
  String phoneNumber,
  @Size(max = 120, message = "Location must be at most 120 characters")
  String location,
  @Size(max = 1000, message = "Bio must be at most 1000 characters")
  String bio,
  @Size(max = 3000000, message = "Avatar data is too large")
  @Pattern(
    regexp = "^(https?://.*|data:image/.+;base64,.*)?$",
    message = "Avatar must be an image URL or uploaded image data"
  )
  String avatarUrl
) {}
