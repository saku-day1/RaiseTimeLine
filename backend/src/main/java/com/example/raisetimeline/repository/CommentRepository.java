package com.example.raisetimeline.repository;

import com.example.raisetimeline.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // N+1防止: JOIN FETCH でコメントとユーザー情報を1クエリで取得
    @Query("""
            SELECT c FROM Comment c
            JOIN FETCH c.user
            WHERE c.post.id = :postId
            ORDER BY c.createdAt DESC
            """)
    List<Comment> findByPostIdWithUser(@Param("postId") Long postId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
