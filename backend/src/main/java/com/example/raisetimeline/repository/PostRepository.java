package com.example.raisetimeline.repository;

import com.example.raisetimeline.dto.response.PostSummaryDto;
import com.example.raisetimeline.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // N+1防止: JOINで user, likes, comments を一括取得し COUNT 集約で射影
    @Query("""
            SELECT new com.example.raisetimeline.dto.response.PostSummaryDto(
                p.id, p.content, p.imageUrl, p.createdAt,
                p.user.id, p.user.username, p.user.profileImageUrl,
                COUNT(DISTINCT l.id), COUNT(DISTINCT c.id)
            )
            FROM Post p
            JOIN p.user u
            LEFT JOIN Like l ON l.post.id = p.id
            LEFT JOIN Comment c ON c.post.id = p.id
            GROUP BY p.id, p.content, p.imageUrl, p.createdAt,
                     p.user.id, p.user.username, p.user.profileImageUrl
            ORDER BY p.createdAt DESC
            """)
    Page<PostSummaryDto> findAllWithCounts(Pageable pageable);

    @Query("""
            SELECT new com.example.raisetimeline.dto.response.PostSummaryDto(
                p.id, p.content, p.imageUrl, p.createdAt,
                p.user.id, p.user.username, p.user.profileImageUrl,
                COUNT(DISTINCT l.id), COUNT(DISTINCT c.id)
            )
            FROM Post p
            JOIN p.user u
            LEFT JOIN Like l ON l.post.id = p.id
            LEFT JOIN Comment c ON c.post.id = p.id
            WHERE p.id = :postId
            GROUP BY p.id, p.content, p.imageUrl, p.createdAt,
                     p.user.id, p.user.username, p.user.profileImageUrl
            """)
    PostSummaryDto findByIdWithCounts(Long postId);

    @Query("""
            SELECT new com.example.raisetimeline.dto.response.PostSummaryDto(
                p.id, p.content, p.imageUrl, p.createdAt,
                p.user.id, p.user.username, p.user.profileImageUrl,
                COUNT(DISTINCT l.id), COUNT(DISTINCT c.id)
            )
            FROM Post p
            JOIN p.user u
            JOIN Follow f ON f.following.id = u.id AND f.follower.id = :currentUserId
            LEFT JOIN Like l ON l.post.id = p.id
            LEFT JOIN Comment c ON c.post.id = p.id
            GROUP BY p.id, p.content, p.imageUrl, p.createdAt,
                     p.user.id, p.user.username, p.user.profileImageUrl
            ORDER BY p.createdAt DESC
            """)
    Page<PostSummaryDto> findFollowingUsersPosts(@Param("currentUserId") Long currentUserId, Pageable pageable);
}
