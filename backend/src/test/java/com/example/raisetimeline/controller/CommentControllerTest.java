package com.example.raisetimeline.controller;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.config.SecurityConfig;
import com.example.raisetimeline.dto.response.CommentResponse;
import com.example.raisetimeline.entity.Comment;
import com.example.raisetimeline.entity.Post;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.exception.ForbiddenException;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CommentService commentService;
    @MockBean
    JwtTokenProvider jwtTokenProvider;

    // --- GET /api/posts/{postId}/comments ---

    @Test
    void getByPost_existingPost_returns200WithList() throws Exception {
        when(commentService.getByPostId(1L)).thenReturn(List.of(buildCommentResponse()));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getByPost_doesNotRequireAuthentication() throws Exception {
        when(commentService.getByPostId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk());
    }

    @Test
    void getByPost_nonExistentPost_returns404() throws Exception {
        when(commentService.getByPostId(99L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/posts/99/comments"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/posts/{postId}/comments ---

    @Test
    void create_authenticated_returns201() throws Exception {
        when(commentService.create(eq(1L), any(), eq(1L))).thenReturn(buildCommentResponse());

        mockMvc.perform(post("/api/posts/1/comments")
                        .with(userAuth(1L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"great post!\"}"))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/posts/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))

                .andExpect(status().isForbidden());
    }

    @Test
    void create_blankContent_returns400() throws Exception {
        mockMvc.perform(post("/api/posts/1/comments")
                        .with(userAuth(1L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}"))

                .andExpect(status().isBadRequest());
    }

    // --- DELETE /api/comments/{commentId} ---

    @Test
    void delete_ownComment_returns204() throws Exception {
        doNothing().when(commentService).delete(5L, 1L);

        mockMvc.perform(delete("/api/comments/5")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notOwner_returns403() throws Exception {
        doThrow(new ForbiddenException("forbidden")).when(commentService).delete(5L, 2L);

        mockMvc.perform(delete("/api/comments/5")
                        .with(userAuth(2L))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_nonExistentComment_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(commentService).delete(99L, 1L);

        mockMvc.perform(delete("/api/comments/99")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/comments/5").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---

    private RequestPostProcessor userAuth(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }

    private CommentResponse buildCommentResponse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setDisplayName("Alice");

        Post post = new Post();
        post.setId(1L);
        post.setContent("post");

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setContent("great post!");
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        return new CommentResponse(comment);
    }
}
