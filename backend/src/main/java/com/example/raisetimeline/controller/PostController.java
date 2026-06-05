package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.request.CreatePostRequest;
import com.example.raisetimeline.dto.response.PostSummaryDto;
import com.example.raisetimeline.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostSummaryDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String tab,
            @AuthenticationPrincipal Long currentUserId) {
        if ("home".equals(tab)) {
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(postService.getFollowingPosts(page, size, currentUserId));
        }
        return ResponseEntity.ok(postService.getAll(page, size, currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostSummaryDto> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(postService.getById(id, currentUserId));
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long currentUserId) throws IOException {
        String imageUrl = postService.uploadPostImage(currentUserId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @PostMapping
    public ResponseEntity<PostSummaryDto> create(
            @Valid @RequestBody CreatePostRequest req,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(req, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        postService.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
