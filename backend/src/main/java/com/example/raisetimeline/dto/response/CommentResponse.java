package com.example.raisetimeline.dto.response;

import com.example.raisetimeline.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    private final Long id;
    private final String content;
    private final LocalDateTime createdAt;
    private final UserInfo user;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.user = new UserInfo(
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getProfileImageUrl()
        );
    }

    @Getter
    public static class UserInfo {
        private final Long id;
        private final String username;
        private final String profileImageUrl;

        public UserInfo(Long id, String username, String profileImageUrl) {
            this.id = id;
            this.username = username;
            this.profileImageUrl = profileImageUrl;
        }
    }
}
