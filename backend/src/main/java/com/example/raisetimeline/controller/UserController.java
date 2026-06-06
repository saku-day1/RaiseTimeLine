package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.request.UpdateProfileRequest;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "マイプロフィール", description = "ログインユーザー自身のプロフィール編集・画像アップロード")
public class UserController {

    private final UserService userService;

    @Operation(summary = "プロフィール更新")
    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(userService.updateProfile(currentUserId, req));
    }

    @Operation(summary = "プロフィール画像アップロード", description = "S3 に画像をアップロードし URL を返します（最大 5MB）。")
    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long currentUserId) throws IOException {
        String imageUrl = userService.uploadProfileImage(currentUserId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @Operation(summary = "ヘッダー画像アップロード", description = "S3 に画像をアップロードし URL を返します（最大 5MB）。")
    @PostMapping("/header-image")
    public ResponseEntity<Map<String, String>> uploadHeaderImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long currentUserId) throws IOException {
        String imageUrl = userService.uploadHeaderImage(currentUserId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}
