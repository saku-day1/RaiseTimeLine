package com.example.raisetimeline.controller;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.config.SecurityConfig;
import com.example.raisetimeline.dto.response.PostSummaryDto;
import com.example.raisetimeline.exception.ForbiddenException;
import com.example.raisetimeline.exception.ResourceNotFoundException;
import com.example.raisetimeline.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

@WebMvcTest(controllers = PostController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostService postService;
    @MockBean
    JwtTokenProvider jwtTokenProvider;

    // --- GET /api/posts?tab=all ---

    @Test
    void getAll_tabAll_anonymousUser_returns200() throws Exception {
        when(postService.getAll(0, 20, null)).thenReturn(Page.empty());

        mockMvc.perform(get("/api/posts?tab=all"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_tabAll_authenticatedUser_returns200() throws Exception {
        when(postService.getAll(anyInt(), anyInt(), eq(1L))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/posts?tab=all").with(userAuth(1L)))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_tabHome_anonymousUser_returns401() throws Exception {
        mockMvc.perform(get("/api/posts?tab=home"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_tabHome_authenticatedUser_returns200() throws Exception {
        when(postService.getFollowingPosts(anyInt(), anyInt(), eq(1L))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/posts?tab=home").with(userAuth(1L)))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_defaultPagination_usesPage0Size20() throws Exception {
        when(postService.getAll(0, 20, null)).thenReturn(Page.empty());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());

        verify(postService).getAll(0, 20, null);
    }

    // --- GET /api/posts/{id} ---

    @Test
    void getById_existingPost_returns200() throws Exception {
        PostSummaryDto dto = buildDto(1L);
        when(postService.getById(1L, null)).thenReturn(dto);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_nonExistentPost_returns404() throws Exception {
        when(postService.getById(99L, null)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/posts/99"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/posts ---

    @Test
    void create_authenticated_returns201() throws Exception {
        PostSummaryDto dto = buildDto(10L);
        when(postService.create(any(), eq(1L))).thenReturn(dto);

        mockMvc.perform(post("/api/posts")
                        .with(userAuth(1L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello world\"}"))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))

                .andExpect(status().isForbidden());
    }

    // --- DELETE /api/posts/{id} ---

    @Test
    void delete_ownPost_returns204() throws Exception {
        doNothing().when(postService).delete(1L, 1L);

        mockMvc.perform(delete("/api/posts/1")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notOwner_returns403() throws Exception {
        doThrow(new ForbiddenException("forbidden")).when(postService).delete(1L, 2L);

        mockMvc.perform(delete("/api/posts/1")
                        .with(userAuth(2L))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_nonExistentPost_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(postService).delete(99L, 1L);

        mockMvc.perform(delete("/api/posts/99")
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/posts/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---

    private RequestPostProcessor userAuth(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }

    private PostSummaryDto buildDto(Long id) {
        return new PostSummaryDto(id, "content", null, LocalDateTime.now(),
                1L, "user", null, 0L, 0L);
    }
}
