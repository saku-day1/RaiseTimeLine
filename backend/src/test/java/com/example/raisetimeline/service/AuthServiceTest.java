package com.example.raisetimeline.service;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.dto.request.LoginRequest;
import com.example.raisetimeline.dto.request.RegisterRequest;
import com.example.raisetimeline.dto.response.AuthResponse;
import com.example.raisetimeline.entity.RefreshToken;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.repository.RefreshTokenRepository;
import com.example.raisetimeline.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    AuthService authService;

    // --- register ---

    @Test
    void register_newEmail_savesUserAndReturnsToken() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(jwtTokenProvider.generateToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.refreshTokenExpiresAt()).thenReturn(LocalDateTime.now().plusDays(7));
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        AuthResponse res = authService.register(buildRegisterRequest("new@example.com"));

        assertThat(res.getToken()).isEqualTo("access-token");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_passwordIsEncoded_notStoredInPlaintext() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$hash");
        when(jwtTokenProvider.generateToken(any())).thenReturn("token");
        when(jwtTokenProvider.refreshTokenExpiresAt()).thenReturn(LocalDateTime.now().plusDays(7));
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        authService.register(buildRegisterRequest("x@example.com"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$hash");
        assertThat(captor.getValue().getPasswordHash()).doesNotContain("password");
    }

    @Test
    void register_duplicateEmail_throwsIllegalStateException() {
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(buildRegisterRequest("dup@example.com")))
                .isInstanceOf(IllegalStateException.class);
        verify(userRepository, never()).save(any());
    }

    // --- login ---

    @Test
    void login_correctCredentials_returnsAuthResponse() {
        User user = createUser(1L, "test@example.com", "encoded");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L)).thenReturn("access");
        when(jwtTokenProvider.refreshTokenExpiresAt()).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse res = authService.login(buildLoginRequest("test@example.com", "password"));

        assertThat(res.getToken()).isEqualTo("access");
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    void login_unknownEmail_throwsIllegalArgumentException() {
        when(userRepository.findByEmail("x@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(buildLoginRequest("x@example.com", "pw")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_wrongPassword_throwsIllegalArgumentException() {
        User user = createUser(1L, "test@example.com", "encoded");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(buildLoginRequest("test@example.com", "wrong")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }

    @Test
    void login_deletesExistingRefreshTokenBeforeCreatingNew() {
        User user = createUser(1L, "test@example.com", "encoded");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L)).thenReturn("token");
        when(jwtTokenProvider.refreshTokenExpiresAt()).thenReturn(LocalDateTime.now().plusDays(7));

        authService.login(buildLoginRequest("test@example.com", "pw"));

        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    // --- refresh ---

    @Test
    void refresh_validToken_returnsNewAuthResponse() {
        User user = createUser(1L, "test@example.com", "hash");
        RefreshToken stored = createRefreshToken(user, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(stored));
        when(jwtTokenProvider.generateToken(1L)).thenReturn("new-access");
        when(jwtTokenProvider.refreshTokenExpiresAt()).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse res = authService.refresh("valid-token");

        assertThat(res.getToken()).isEqualTo("new-access");
        verify(refreshTokenRepository).delete(stored);
    }

    @Test
    void refresh_expiredToken_throwsAndDeletesToken() {
        User user = createUser(1L, "test@example.com", "hash");
        RefreshToken expired = createRefreshToken(user, LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("expired"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    void refresh_unknownToken_throwsIllegalArgumentException() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- helpers ---

    private RegisterRequest buildRegisterRequest(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("password");
        req.setUsername("user");
        req.setDisplayName("User");
        return req;
    }

    private LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private User createUser(Long id, String email, String passwordHash) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setUsername("user");
        u.setDisplayName("User");
        return u;
    }

    private RefreshToken createRefreshToken(User user, LocalDateTime expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken("token-value");
        rt.setExpiresAt(expiresAt);
        return rt;
    }
}
