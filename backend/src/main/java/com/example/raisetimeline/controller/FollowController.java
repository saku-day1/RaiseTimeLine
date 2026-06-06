package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.response.FollowStatusResponse;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.dto.response.UserSummaryDto;
import com.example.raisetimeline.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "ユーザー・フォロー", description = "ユーザープロフィール取得・フォロー操作・ユーザー検索")
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "ユーザープロフィール取得")
    @SecurityRequirements
    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserProfileDto> getUserProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.getUserProfile(id, currentUserId));
    }

    @Operation(summary = "フォロワー一覧取得")
    @SecurityRequirements
    @GetMapping("/api/users/{id}/followers")
    public ResponseEntity<List<UserSummaryDto>> getFollowers(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.getFollowers(id, currentUserId));
    }

    @Operation(summary = "フォロー中一覧取得")
    @SecurityRequirements
    @GetMapping("/api/users/{id}/following")
    public ResponseEntity<List<UserSummaryDto>> getFollowing(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.getFollowing(id, currentUserId));
    }

    @Operation(summary = "ユーザー検索", description = "username または displayName でユーザーを検索します（認証必須）。")
    @GetMapping("/api/users/search")
    public ResponseEntity<List<UserSummaryDto>> searchUsers(
            @RequestParam String q,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.searchUsers(q, currentUserId));
    }

    @Operation(summary = "フォロー")
    @PostMapping("/api/users/{id}/follow")
    public ResponseEntity<FollowStatusResponse> follow(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.follow(id, currentUserId));
    }

    @Operation(summary = "アンフォロー")
    @DeleteMapping("/api/users/{id}/follow")
    public ResponseEntity<FollowStatusResponse> unfollow(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.unfollow(id, currentUserId));
    }

    @Operation(summary = "フォロー状態取得")
    @SecurityRequirements
    @GetMapping("/api/users/{id}/follow/status")
    public ResponseEntity<FollowStatusResponse> getStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.getStatus(id, currentUserId));
    }
}
