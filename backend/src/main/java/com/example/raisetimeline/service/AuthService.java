package com.example.raisetimeline.service;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.dto.request.LoginRequest;
import com.example.raisetimeline.dto.request.RegisterRequest;
import com.example.raisetimeline.dto.response.AuthResponse;
import com.example.raisetimeline.entity.RefreshToken;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.repository.RefreshTokenRepository;
import com.example.raisetimeline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("このメールアドレスはすでに使用されています");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setDisplayName(req.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
        log.info("event=auth.register.success userId={}", user.getId());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("event=auth.login.failure reason=email_not_found");
                    return new IllegalArgumentException("メールアドレスまたはパスワードが正しくありません");
                });

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            log.warn("event=auth.login.failure reason=password_mismatch userId={}", user.getId());
            throw new IllegalArgumentException("メールアドレスまたはパスワードが正しくありません");
        }

        refreshTokenRepository.deleteByUserId(user.getId());
        log.info("event=auth.login.success userId={}", user.getId());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String token) {
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効なリフレッシュトークンです"));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            log.warn("event=auth.refresh.failure reason=expired userId={}", stored.getUser().getId());
            throw new IllegalArgumentException("リフレッシュトークンの有効期限が切れています");
        }

        User user = stored.getUser();
        refreshTokenRepository.delete(stored);
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateToken(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(jwtTokenProvider.refreshTokenExpiresAt());
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getUsername(), user.getDisplayName());
    }
}
