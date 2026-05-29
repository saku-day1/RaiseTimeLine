package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.response.LikeStatusResponse;
import com.example.raisetimeline.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<LikeStatusResponse> like(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(likeService.like(postId, currentUserId));
    }

    @DeleteMapping
    public ResponseEntity<LikeStatusResponse> unlike(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(likeService.unlike(postId, currentUserId));
    }

    @GetMapping("/me")
    public ResponseEntity<LikeStatusResponse> getStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(likeService.getStatus(postId, currentUserId));
    }
}
