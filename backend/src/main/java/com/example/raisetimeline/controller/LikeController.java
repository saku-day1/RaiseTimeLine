package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.response.LikeStatusResponse;
import com.example.raisetimeline.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
@Tag(name = "いいね", description = "投稿へのいいね追加・削除・状態取得")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "いいね追加")
    @PostMapping
    public ResponseEntity<LikeStatusResponse> like(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(likeService.like(postId, currentUserId));
    }

    @Operation(summary = "いいね削除")
    @DeleteMapping
    public ResponseEntity<LikeStatusResponse> unlike(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(likeService.unlike(postId, currentUserId));
    }

    @Operation(summary = "いいね状態取得", description = "現在のユーザーがこの投稿をいいねしているか確認します。")
    @GetMapping("/me")
    public ResponseEntity<LikeStatusResponse> getStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(likeService.getStatus(postId, currentUserId));
    }
}
