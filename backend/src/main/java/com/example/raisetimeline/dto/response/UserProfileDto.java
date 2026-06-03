package com.example.raisetimeline.dto.response;

public record UserProfileDto(
        Long id,
        String username,
        String displayName,
        String profileImageUrl,
        String headerImageUrl,
        String bio,
        long followerCount,
        long followingCount,
        boolean following,
        boolean followedBack
) {}
