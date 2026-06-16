package com.example.raisetimeline.controller;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.config.SecurityConfig;
import com.example.raisetimeline.dto.response.LikeStatusResponse;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.service.LikeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LikeController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class LikeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LikeService likeService;
    @MockBean
    JwtTokenProvider jwtTokenProvider;

    // --- POST /api/posts/{postId}/likes ---

    @Test
    void like_authenticated_returns200WithLikedTrue() throws Exception {
        when(likeService.like(1L, 1L)).thenReturn(new LikeStatusResponse(true, 1L));

        mockMvc.perform(post("/api/posts/1/likes")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void like_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/posts/1/likes").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void like_nonExistentPost_returns404() throws Exception {
        when(likeService.like(99L, 1L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(post("/api/posts/99/likes")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/posts/{postId}/likes ---

    @Test
    void unlike_authenticated_returns200WithLikedFalse() throws Exception {
        when(likeService.unlike(1L, 1L)).thenReturn(new LikeStatusResponse(false, 0L));

        mockMvc.perform(delete("/api/posts/1/likes")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    void unlike_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/posts/1/likes").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/posts/{postId}/likes/me ---

    @Test
    void getStatus_authenticated_returns200() throws Exception {
        when(likeService.getStatus(1L, 1L)).thenReturn(new LikeStatusResponse(true, 5L));

        mockMvc.perform(get("/api/posts/1/likes/me").with(userAuth(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void getStatus_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/posts/1/likes/me"))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---

    private RequestPostProcessor userAuth(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }
}
