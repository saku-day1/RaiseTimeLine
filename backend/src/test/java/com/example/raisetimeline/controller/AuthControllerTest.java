package com.example.raisetimeline.controller;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.config.SecurityConfig;
import com.example.raisetimeline.dto.response.AuthResponse;
import com.example.raisetimeline.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthService authService;
    @MockBean
    JwtTokenProvider jwtTokenProvider;

    // --- register ---

    @Test
    void register_validRequest_returns201AndToken() throws Exception {
        when(authService.register(any())).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","email":"alice@example.com",
                                 "password":"password1","displayName":"Alice"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("access-token"));
    }

    @Test
    void register_validRequest_setsRefreshTokenCookie() throws Exception {
        when(authService.register(any())).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","email":"alice@example.com",
                                 "password":"password1","displayName":"Alice"}"""))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken")));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(any())).thenThrow(new IllegalStateException("メールアドレスはすでに使用されています"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","email":"dup@example.com",
                                 "password":"password1","displayName":"Alice"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","email":"not-an-email",
                                 "password":"password1","displayName":"Alice"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","email":"alice@example.com",
                                 "password":"short","displayName":"Alice"}"""))
                .andExpect(status().isBadRequest());
    }

    // --- login ---

    @Test
    void login_validCredentials_returns200AndToken() throws Exception {
        when(authService.login(any())).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"password1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"));
    }

    @Test
    void login_wrongPassword_returns400() throws Exception {
        when(authService.login(any())).thenThrow(new IllegalArgumentException("認証失敗"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"wrong\"}"))

                .andExpect(status().isBadRequest());
    }

    // --- refresh ---

    @Test
    void refresh_validCookie_returns200AndNewToken() throws Exception {
        when(authService.refresh("valid-token")).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "valid-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"));
    }

    @Test
    void refresh_noCookie_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // --- logout ---

    @Test
    void logout_returns204AndClearsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    // --- helpers ---

    private AuthResponse buildAuthResponse() {
        return new AuthResponse("access-token", "refresh-token", 1L, "alice", "Alice");
    }
}
