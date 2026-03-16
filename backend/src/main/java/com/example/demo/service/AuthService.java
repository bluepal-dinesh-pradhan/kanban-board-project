package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final InvitationRepository invitationRepository;
    private final BoardMemberRepository boardMemberRepository;

    @Transactional
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

        // Auto-accept any pending invitations for this email
        List<Invitation> pendingInvitations = invitationRepository.findByEmailAndStatus(
                user.getEmail(), Invitation.InvitationStatus.PENDING);
        
        for (Invitation invitation : pendingInvitations) {
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

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }
}