package com.example.raisetimeline.dto.response;

public record FollowStatusResponse(
        boolean following,
        long followerCount,
        long followingCount
) {}
