package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.request.UpdateProfileRequest;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.service.UserService;
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
public class UserController {

    private final UserService userService;

    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(userService.updateProfile(currentUserId, req));
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long currentUserId) throws IOException {
        String imageUrl = userService.uploadProfileImage(currentUserId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @PostMapping("/header-image")
    public ResponseEntity<Map<String, String>> uploadHeaderImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long currentUserId) throws IOException {
        String imageUrl = userService.uploadHeaderImage(currentUserId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}
