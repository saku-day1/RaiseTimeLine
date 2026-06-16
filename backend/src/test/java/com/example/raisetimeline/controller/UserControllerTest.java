package com.example.raisetimeline.controller;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.config.SecurityConfig;
import com.example.raisetimeline.dto.response.UserProfileDto;
import com.example.raisetimeline.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;
    @MockBean
    JwtTokenProvider jwtTokenProvider;

    // --- PUT /api/users/me ---

    @Test
    void updateProfile_authenticated_returns200() throws Exception {
        UserProfileDto dto = buildProfile(1L);
        when(userService.updateProfile(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/users/me")
                        .with(userAuth(1L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"New Name\",\"bio\":\"My bio\"}"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateProfile_unauthenticated_returns403() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Name\"}"))

                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_blankDisplayName_returns400() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .with(userAuth(1L))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"\"}"))

                .andExpect(status().isBadRequest());
    }

    // --- POST /api/users/me/image ---

    @Test
    void uploadImage_authenticated_returns200() throws Exception {
        when(userService.uploadProfileImage(eq(1L), any())).thenReturn("https://s3.example.com/img.jpg");

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/api/users/me/image")
                        .file(file)
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://s3.example.com/img.jpg"));
    }

    @Test
    void uploadImage_unauthenticated_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1});
        mockMvc.perform(multipart("/api/users/me/image")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/users/me/header-image ---

    @Test
    void uploadHeaderImage_authenticated_returns200() throws Exception {
        when(userService.uploadHeaderImage(eq(1L), any())).thenReturn("https://s3.example.com/header.jpg");

        MockMultipartFile file = new MockMultipartFile("file", "header.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/api/users/me/header-image")
                        .file(file)
                        .with(userAuth(1L))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://s3.example.com/header.jpg"));
    }

    @Test
    void uploadHeaderImage_unauthenticated_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "header.jpg", "image/jpeg", new byte[]{1});
        mockMvc.perform(multipart("/api/users/me/header-image")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---

    private RequestPostProcessor userAuth(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }

    private UserProfileDto buildProfile(Long id) {
        return new UserProfileDto(id, "user", "User", null, null, null, 0L, 0L, false, false);
    }
}
