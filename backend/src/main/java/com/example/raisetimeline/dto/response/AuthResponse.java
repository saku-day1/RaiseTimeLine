package com.example.raisetimeline.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @JsonIgnore
    private String refreshToken;
    private Long userId;
    private String username;
    private String displayName;

    public AuthResponse withoutRefreshToken() {
        return new AuthResponse(token, null, userId, username, displayName);
    }
}
