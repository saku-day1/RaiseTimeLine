package com.example.raisetimeline.repository;

import com.example.raisetimeline.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    // N+1防止: タイムラインのいいね済み投稿IDを IN句で一括取得
    @Query("SELECT l.post.id FROM Like l WHERE l.user.id = :userId AND l.post.id IN :postIds")
    Set<Long> findLikedPostIdsByUserIdAndPostIds(@Param("userId") Long userId,
                                                  @Param("postIds") List<Long> postIds);
}
