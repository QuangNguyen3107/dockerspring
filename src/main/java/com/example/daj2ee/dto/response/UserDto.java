package com.example.daj2ee.dto.response;

import com.example.daj2ee.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
  Long id,
  String username,
  String email,
  int points,
  String fullName,
  String phoneNumber,
  String location,
  String avatarUrl,
  String bio,
  List<String> roles,
  boolean enabled,
  LocalDateTime createdAt
) {
  public UserDto(
    Long id,
    String username,
    String email,
    int points,
    String fullName,
    String phoneNumber,
    String location,
    String avatarUrl,
    String bio,
    List<String> roles,
    boolean enabled,
    LocalDateTime createdAt
  ) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.points = points;
    this.fullName = fullName;
    this.phoneNumber = phoneNumber;
    this.location = location;
    this.avatarUrl = avatarUrl;
    this.bio = bio;
    this.roles = roles == null ? Collections.emptyList() : List.copyOf(roles);
    this.enabled = enabled;
    this.createdAt = createdAt;
  }

  public static UserDto fromEntity(User user) {
    if (user == null) return null;
    List<String> roleList = user.getRoleList();
    return new UserDto(
      user.getId(),
      user.getUsername(),
      user.getEmail(),
      user.getPoints(),
      user.getFullName(),
      user.getPhoneNumber(),
      user.getLocation(),
      user.getAvatarUrl(),
      user.getBio(),
      roleList == null ? Collections.emptyList() : roleList,
      user.isEnabled(),
      user.getCreatedAt()
    );
  }
}
