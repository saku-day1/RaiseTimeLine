package com.example.raisetimeline.controller;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.config.SecurityConfig;
import com.example.raisetimeline.dto.response.FollowStatusResponse;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.dto.response.UserSummaryDto;
import com.example.raisetimeline.exception.ForbiddenException;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.service.FollowService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FollowController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class FollowControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FollowService followService;
    @MockBean
    JwtTokenProvider jwtTokenProvider;

    // --- GET /api/users/{id} ---

    @Test
    void getUserProfile_public_returns200() throws Exception {
        when(followService.getUserProfile(2L, null)).thenReturn(buildProfile(2L));

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getUserProfile_notFound_returns404() throws Exception {
        when(followService.getUserProfile(eq(99L), any())).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // --- GET /api/users/{id}/followers ---

    @Test
    void getFollowers_public_returns200() throws Exception {
        when(followService.getFollowers(eq(2L), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/users/2/followers"))
                .andExpect(status().isOk());
    }

    // --- GET /api/users/{id}/following ---

    @Test
    void getFollowing_public_returns200() throws Exception {
        when(followService.getFollowing(eq(2L), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/users/2/following"))
                .andExpect(status().isOk());
    }

    // --- GET /api/users/search ---

    @Test
    void searchUsers_authenticated_returns200WithList() throws Exception {
        when(followService.searchUsers("alice", 1L)).thenReturn(List.of(
                new UserSummaryDto(2L, "alice", "Alice", null, false)));

        mockMvc.perform(get("/api/users/search?q=alice").with(userAuth(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void searchUsers_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/users/search?q=alice"))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/users/{id}/follow ---

    @Test
    void follow_authenticated_returns200() throws Exception {
        when(followService.follow(2L, 1L)).thenReturn(new FollowStatusResponse(true, 1L, 0L));

        mockMvc.perform(post("/api/users/2/follow")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true));
    }

    @Test
    void follow_selfFollow_returns403() throws Exception {
        when(followService.follow(1L, 1L)).thenThrow(new ForbiddenException("self follow"));

        mockMvc.perform(post("/api/users/1/follow")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void follow_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/users/2/follow").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- DELETE /api/users/{id}/follow ---

    @Test
    void unfollow_authenticated_returns200() throws Exception {
        when(followService.unfollow(2L, 1L)).thenReturn(new FollowStatusResponse(false, 0L, 0L));

        mockMvc.perform(delete("/api/users/2/follow")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));
    }

    @Test
    void unfollow_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/2/follow").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/users/{id}/follow/status ---

    @Test
    void getFollowStatus_returns200() throws Exception {
        when(followService.getStatus(eq(2L), any())).thenReturn(new FollowStatusResponse(false, 0L, 0L));

        mockMvc.perform(get("/api/users/2/follow/status"))
                .andExpect(status().isOk());
    }

    // --- helpers ---

    private RequestPostProcessor userAuth(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }

    private UserProfileDto buildProfile(Long id) {
        return new UserProfileDto(id, "user" + id, "User " + id, null, null, null, 0L, 0L, false, false);
    }
}
