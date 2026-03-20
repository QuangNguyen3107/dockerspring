package com.example.daj2ee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * User entity for authentication and basic profile information.
 */
@Entity
@Table(
  name = "users",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = { "username" }),
    @UniqueConstraint(columnNames = { "email" }),
  }
)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String username;

  @NotBlank
  @Column(nullable = false)
  private String password;

  @Email
  @Column(nullable = false, unique = true)
  private String email;

  @Column
  private String roles;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(columnDefinition = "int default 0")
  private int points = 0;

  @Column(length = 120)
  private String fullName;

  @Column(length = 20)
  private String phoneNumber;

  @Column(length = 120)
  private String location;

  @Column(columnDefinition = "TEXT")
  private String avatarUrl;

  @Column(name = "avatar_data", columnDefinition = "TEXT")
  private String avatarData;

  @Column(length = 1000)
  private String bio;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public User() {
    this.createdAt = LocalDateTime.now();
  }

  public User(String username, String password, String email, String roles) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.roles = roles;
    this.enabled = true;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRoles() {
    return roles;
  }

  public void setRoles(String roles) {
    this.roles = roles;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getAvatarUrl() {
    return avatarData != null ? avatarData : avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    if (avatarUrl != null && avatarUrl.startsWith("data:image/")) {
      this.avatarData = avatarUrl;
      this.avatarUrl = null;
      return;
    }
    this.avatarUrl = avatarUrl;
    this.avatarData = null;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Convenience method to return roles as a List.
   */
  public List<String> getRoleList() {
    if (roles == null || roles.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.stream(roles.split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .toList();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    User user = (User) o;
    return (
      Objects.equals(id, user.id) && Objects.equals(username, user.username)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username);
  }

  @Override
  public String toString() {
    return (
      "User{" +
      "id=" +
      id +
      ", username='" +
      username +
      '\'' +
      ", email='" +
      email +
      '\'' +
      ", roles='" +
      roles +
      '\'' +
      ", enabled=" +
      enabled +
      ", createdAt=" +
      createdAt +
      '}'
    );
  }
}
