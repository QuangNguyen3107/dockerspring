package com.example.daj2ee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Represents a coding problem/challenge.
 * This entity replaces the static Tutorial entity to provide interactive coding tasks
 * that users can solve and submit code for.
 */
@Entity
@Table(
  name = "problems",
  indexes = {
    @Index(
      name = "idx_problems_published_created_at",
      columnList = "published, created_at"
    ),
    @Index(name = "idx_problems_published", columnList = "published"),
  }
)
public class Problem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String description;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String constraints;

  @Column
  private String difficulty; // e.g., EASY, MEDIUM, HARD

  @Column
  private String tags; // comma-separated tags


  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "boilerplates", columnDefinition = "jsonb")
  private Map<Integer, String> boilerplates = new HashMap<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @Column(nullable = false)
  private boolean published = false;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Problem() {}

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // --- Getters and Setters ---

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getConstraints() {
    return constraints;
  }

  public void setConstraints(String constraints) {
    this.constraints = constraints;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public Map<Integer, String> getBoilerplates() {
    return boilerplates;
  }

  public void setBoilerplates(Map<Integer, String> boilerplates) {
    this.boilerplates = boilerplates != null ? boilerplates : new HashMap<>();
  }

  /**
   * Convenience method — returns the boilerplate for a specific language ID,
   * or {@code null} if no boilerplate has been defined for that language.
   *
   * @param languageId the Judge0 language ID
   * @return the boilerplate source code, or null
   */
  public String getBoilerplateForLanguage(int languageId) {
    return boilerplates.get(languageId);
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public boolean isPublished() {
    return published;
  }

  public void setPublished(boolean published) {
    this.published = published;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Problem problem = (Problem) o;
    return Objects.equals(id, problem.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return (
      "Problem{" +
      "id=" +
      id +
      ", title='" +
      title +
      '\'' +
      ", difficulty='" +
      difficulty +
      '\'' +
      ", author=" +
      (author != null ? author.getUsername() : null) +
      ", published=" +
      published +
      '}'
    );
  }
}
