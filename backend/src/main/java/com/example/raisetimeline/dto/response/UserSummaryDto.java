package com.example.raisetimeline.dto.response;

public record UserSummaryDto(
        Long id,
        String username,
        String displayName,
        String profileImageUrl,
        boolean following
) {}
