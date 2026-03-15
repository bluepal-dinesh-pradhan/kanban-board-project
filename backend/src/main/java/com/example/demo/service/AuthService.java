package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .build();
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(AuthRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest req) {
        String token = req.getRefreshToken();
        if (!jwtService.isValid(token)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtService.getEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getId(), user.getEmail());
        return new AuthResponse(access, refresh, UserDto.from(user));
    }
}

