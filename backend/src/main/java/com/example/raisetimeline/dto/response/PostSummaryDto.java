package com.example.raisetimeline.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostSummaryDto {

    private final Long id;
    private final String content;
    private final String imageUrl;
    private final LocalDateTime createdAt;
    private final Long userId;
    private final String username;
    private final String profileImageUrl;
    private final long likeCount;
    private final long commentCount;
    private boolean liked;

    public PostSummaryDto(Long id, String content, String imageUrl, LocalDateTime createdAt,
                          Long userId, String username, String profileImageUrl,
                          long likeCount, long commentCount) {
        this.id = id;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.userId = userId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
