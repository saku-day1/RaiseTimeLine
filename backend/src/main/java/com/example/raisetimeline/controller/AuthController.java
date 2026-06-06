package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.request.LoginRequest;
import com.example.raisetimeline.dto.request.RegisterRequest;
import com.example.raisetimeline.dto.response.AuthResponse;
import com.example.raisetimeline.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "認証", description = "ユーザー登録・ログイン・トークン管理")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-expiration-days}")
    private int refreshExpirationDays;

    @Operation(summary = "ユーザー登録", description = "新規ユーザーを作成します。RefreshToken は HttpOnly Cookie にセットされます。")
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletResponse response) {
        AuthResponse body = authService.register(req);
        setRefreshTokenCookie(response, body.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(body.withoutRefreshToken());
    }

    @Operation(summary = "ログイン", description = "メールアドレスとパスワードで認証し、アクセストークンを返します。")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletResponse response) {
        AuthResponse body = authService.login(req);
        setRefreshTokenCookie(response, body.getRefreshToken());
        return ResponseEntity.ok(body.withoutRefreshToken());
    }

    @Operation(summary = "トークン更新", description = "HttpOnly Cookie の refreshToken を使ってアクセストークンを再発行します。")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse body = authService.refresh(refreshToken);
        setRefreshTokenCookie(response, body.getRefreshToken());
        return ResponseEntity.ok(body.withoutRefreshToken());
    }

    @Operation(summary = "ログアウト", description = "refreshToken Cookie をクリアしてセッションを終了します。")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(Duration.ofDays(refreshExpirationDays))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
