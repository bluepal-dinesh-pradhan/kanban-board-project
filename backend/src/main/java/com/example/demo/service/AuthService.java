package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final InvitationRepository invitationRepository;
    private final BoardMemberRepository boardMemberRepository;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        log.info("Registering user {}", req.getEmail());
        if (userRepository.existsByEmail(req.getEmail())) {
            log.warn("Registration attempt with existing email {}", req.getEmail());
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .build();
        user = userRepository.save(user);

        // Auto-accept any pending invitations for this email
        List<Invitation> pendingInvitations = invitationRepository.findByEmailAndStatus(
                user.getEmail(), Invitation.InvitationStatus.PENDING);
        log.debug("Found {} pending invitations for {}", pendingInvitations.size(), user.getEmail());
        
        for (Invitation invitation : pendingInvitations) {
            if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                continue;
            }
            if (!boardMemberRepository.existsByBoardIdAndUserId(
                    invitation.getBoard().getId(), user.getId())) {
                BoardMember member = BoardMember.builder()
                        .board(invitation.getBoard())
                        .user(user)
                        .role(invitation.getRole())
                        .build();
                boardMemberRepository.save(member);
            }
            invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);
        }

        AuthResponse response = buildAuthResponse(user);
        log.info("User {} registered successfully with id {}", user.getEmail(), user.getId());
        return response;
    }

    public AuthResponse login(AuthRequest req) {
        log.info("Login attempt for {}", req.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        log.debug("Authentication succeeded for {}", req.getEmail());
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for {}", req.getEmail());
                    return new RuntimeException("User not found");
                });
        AuthResponse response = buildAuthResponse(user);
        log.info("Login successful for user {}", user.getId());
        return response;
    }

    public AuthResponse refresh(RefreshTokenRequest req) {
        log.info("Refreshing access token");
        String token = req.getRefreshToken();
        if (!jwtService.isValid(token)) {
            log.warn("Refresh token validation failed");
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtService.getEmail(token);
        log.debug("Refresh token validated for {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Refresh failed: user not found for {}", email);
                    return new RuntimeException("User not found");
                });
        AuthResponse response = buildAuthResponse(user);
        log.info("Token refreshed successfully for user {}", user.getId());
        return response;
    }

    private AuthResponse buildAuthResponse(User user) {
        log.info("Building auth response for user {}", user.getId());
        log.debug("Generating tokens for user {}", user.getId());
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getId(), user.getEmail());
        AuthResponse response = new AuthResponse(access, refresh, UserDto.from(user));
        log.info("Auth response built successfully for user {}", user.getId());
        return response;
    }

    public boolean userExists(String email) {
        log.info("Checking if user exists for {}", email);
        boolean exists = userRepository.existsByEmail(email);
        log.debug("User exists check for {} returned {}", email, exists);
        log.info("User existence check completed for {}", email);
        return exists;
    }
}
