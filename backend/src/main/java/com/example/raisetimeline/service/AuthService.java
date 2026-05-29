package com.example.raisetimeline.service;

import com.example.raisetimeline.config.JwtTokenProvider;
import com.example.raisetimeline.dto.request.LoginRequest;
import com.example.raisetimeline.dto.request.RegisterRequest;
import com.example.raisetimeline.dto.response.AuthResponse;
import com.example.raisetimeline.entity.User;
import com.example.raisetimeline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
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
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("メールアドレスまたはパスワードが正しくありません"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが正しくありません");
        }

        String token = jwtTokenProvider.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}
