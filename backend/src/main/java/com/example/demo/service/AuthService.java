package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.BoardMember;
import com.example.demo.entity.Invitation;
import com.example.demo.entity.User;
import com.example.demo.repository.BoardMemberRepository;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        
        // Validate name is not empty
        if (req.getFullName() == null || req.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }

        // Validate email format
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (req.getEmail() == null || !req.getEmail().matches(emailPattern)) {
            throw new RuntimeException("Valid email is required");
        }

        // Check if email already registered
        if (userRepository.existsByEmail(req.getEmail())) {
            log.warn("Registration attempt with existing email {}", req.getEmail());
            throw new RuntimeException("Email is already registered");
        }

        // Validate password strength
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$";
        if (req.getPassword() == null || !req.getPassword().matches(passwordPattern)) {
            throw new RuntimeException("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            log.warn("Login failed: invalid credentials for {}", req.getEmail());
            throw e;
        } catch (AuthenticationException e) {
            log.warn("Login failed for {}: {}", req.getEmail(), e.getMessage());
            throw new RuntimeException("Invalid email or password");
        }
        
        log.debug("Authentication succeeded for {}", req.getEmail());
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for {}", req.getEmail());
                    return new RuntimeException("Invalid email or password");
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
