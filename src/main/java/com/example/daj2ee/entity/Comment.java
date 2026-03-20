package com.example.daj2ee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "problem_id", nullable = false)
  private Problem problem;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(
    name = "upvotes",
    nullable = false,
    columnDefinition = "integer default 0"
  )
  private Integer upvotes = 0;

  @Column(
    name = "downvotes",
    nullable = false,
    columnDefinition = "integer default 0"
  )
  private Integer downvotes = 0;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "parent_id", nullable = true)
  private Comment parent;

  public Comment() {}

  public Comment(Problem problem, User author, String content) {
    this.problem = problem;
    this.author = author;
    this.content = content;
    this.createdAt = LocalDateTime.now();
    this.parent = null;
  }

  public Comment(Problem problem, User author, String content, Comment parent) {
    this.problem = problem;
    this.author = author;
    this.content = content;
    this.createdAt = LocalDateTime.now();
    this.parent = parent;
  }

  // Getters and setters
  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Problem getProblem() {
    return this.problem;
  }

  public void setProblem(Problem problem) {
    this.problem = problem;
  }

  public User getAuthor() {
    return this.author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Integer getUpvotes() {
    return this.upvotes;
  }

  public void setUpvotes(Integer upvotes) {
    this.upvotes = upvotes;
  }

  public Integer getDownvotes() {
    return this.downvotes;
  }

  public void setDownvotes(Integer downvotes) {
    this.downvotes = downvotes;
  }

  public Comment getParent() {
    return this.parent;
  }

  public void setParent(Comment parent) {
    this.parent = parent;
  }
}
