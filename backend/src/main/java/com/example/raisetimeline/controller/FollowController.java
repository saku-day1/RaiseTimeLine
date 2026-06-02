package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.response.FollowStatusResponse;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.dto.response.UserSummaryDto;
import com.example.raisetimeline.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserProfileDto> getUserProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.getUserProfile(id, currentUserId));
    }

    @GetMapping("/api/users/{id}/followers")
    public ResponseEntity<List<UserSummaryDto>> getFollowers(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.getFollowers(id, currentUserId));
    }

    @GetMapping("/api/users/{id}/following")
    public ResponseEntity<List<UserSummaryDto>> getFollowing(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.getFollowing(id, currentUserId));
    }

    @GetMapping("/api/users/search")
    public ResponseEntity<List<UserSummaryDto>> searchUsers(
            @RequestParam String q,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.searchUsers(q, currentUserId));
    }

    @PostMapping("/api/users/{id}/follow")
    public ResponseEntity<FollowStatusResponse> follow(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.follow(id, currentUserId));
    }

    @DeleteMapping("/api/users/{id}/follow")
    public ResponseEntity<FollowStatusResponse> unfollow(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.unfollow(id, currentUserId));
    }

    @GetMapping("/api/users/{id}/follow/status")
    public ResponseEntity<FollowStatusResponse> getStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(followService.getStatus(id, currentUserId));
    }
}
