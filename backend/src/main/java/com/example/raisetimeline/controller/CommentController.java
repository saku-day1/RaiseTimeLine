package com.example.raisetimeline.controller;

import com.example.raisetimeline.dto.request.CreateCommentRequest;
import com.example.raisetimeline.dto.response.CommentResponse;
import com.example.raisetimeline.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "コメント", description = "投稿へのコメント作成・取得・削除")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "コメント一覧取得")
    @SecurityRequirements
    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getByPostId(postId));
    }

    @Operation(summary = "コメント作成")
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> create(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest req,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(postId, req, currentUserId));
    }

    @Operation(summary = "コメント削除", description = "自分のコメントのみ削除できます。")
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long currentUserId) {
        commentService.delete(commentId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
