package com.example.daj2ee.repository;

import com.example.daj2ee.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query(
    "SELECT c FROM Comment c JOIN FETCH c.author WHERE c.problem.id = :problemId ORDER BY c.createdAt DESC"
  )
  List<Comment> findByProblemId(@Param("problemId") Long problemId);

  @Query(
    "SELECT c FROM Comment c JOIN FETCH c.author WHERE c.problem.id = :problemId AND c.parent IS NULL ORDER BY c.createdAt DESC"
  )
  List<Comment> findTopLevelCommentsByProblemId(
    @Param("problemId") Long problemId
  );

  @Query(
    "SELECT c FROM Comment c JOIN FETCH c.author WHERE c.parent.id = :parentId ORDER BY c.createdAt DESC"
  )
  List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);
}
