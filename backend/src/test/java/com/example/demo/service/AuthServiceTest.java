package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.BoardMemberRepository;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private BoardMemberRepository boardMemberRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldThrow_whenNameEmpty() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("");
        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_shouldThrow_whenEmailInvalid() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Name");
        req.setEmail("invalid");
        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Name");
        req.setEmail("test@ex.com");
        when(userRepository.existsByEmail("test@ex.com")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_shouldThrow_whenPasswordWeak() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Name");
        req.setEmail("test@ex.com");
        req.setPassword("weak");
        when(userRepository.existsByEmail("test@ex.com")).thenReturn(false);
        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_shouldSuccess_withPendingInvites() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Name");
        req.setEmail("test@ex.com");
        req.setPassword("Valid123!");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@ex.com");

        Board board = new Board();
        board.setId(10L);

        Invitation invite1 = Invitation.builder().id(1L).board(board).role(BoardMember.Role.EDITOR).status(Invitation.InvitationStatus.PENDING).build();
        Invitation invite2 = Invitation.builder().id(2L).board(board).role(BoardMember.Role.EDITOR).status(Invitation.InvitationStatus.PENDING).expiresAt(LocalDateTime.now().minusDays(1)).build();
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(invitationRepository.findByEmailAndStatus(anyString(), eq(Invitation.InvitationStatus.PENDING)))
                .thenReturn(List.of(invite1, invite2));
        when(boardMemberRepository.existsByBoardIdAndUserId(10L, 1L)).thenReturn(false);

        authService.register(req);

        verify(boardMemberRepository, times(1)).save(any());
        assertEquals(Invitation.InvitationStatus.ACCEPTED, invite1.getStatus());
        assertNotEquals(Invitation.InvitationStatus.ACCEPTED, invite2.getStatus());
    }

    @Test
    void login_shouldThrow_whenBadCredentials() {
        AuthRequest req = new AuthRequest();
        req.setEmail("e"); req.setPassword("p");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("err"));
        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void login_shouldSuccess() {
        AuthRequest req = new AuthRequest();
        req.setEmail("test@ex.com"); req.setPassword("pass");
        User user = new User();
        user.setId(1L);
        user.setEmail("test@ex.com");

        when(userRepository.findByEmail("test@ex.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(anyLong(), anyString())).thenReturn("access");
        when(jwtService.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh");

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("access", resp.getAccessToken());
    }

    @Test
    void refresh_shouldThrow_whenInvalid() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("bad");
        when(jwtService.isValid("bad")).thenReturn(false);
        assertThrows(BadRequestException.class, () -> authService.refresh(req));
    }

    @Test
    void refresh_shouldSuccess() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("good");
        User user = new User();
        user.setId(1L);
        user.setEmail("e");

        when(jwtService.isValid("good")).thenReturn(true);
        when(jwtService.getEmail("good")).thenReturn("e");
        when(userRepository.findByEmail("e")).thenReturn(Optional.of(user));

        authService.refresh(req);
        verify(jwtService).generateAccessToken(anyLong(), anyString());
    }

    @Test
    void forgotPassword_shouldReturnSilently_whenUserMissing() {
        when(userRepository.existsByEmail("e")).thenReturn(false);
        authService.forgotPassword("e");
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void forgotPassword_shouldSuccess() {
        when(userRepository.existsByEmail("e")).thenReturn(true);
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);
        authService.forgotPassword("e");
        verify(passwordResetTokenRepository).deleteByEmail("e");
        verify(passwordResetTokenRepository).save(any());
    }

    @Test
    void resetPassword_shouldThrow_whenTokenInvalid() {
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("tok")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> authService.resetPassword("tok", "Valid123!"));
    }

    @Test
    void resetPassword_shouldThrow_whenExpired() {
        PasswordResetToken token = PasswordResetToken.builder().expiresAt(LocalDateTime.now().minusDays(1)).build();
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("tok")).thenReturn(Optional.of(token));
        assertThrows(BadRequestException.class, () -> authService.resetPassword("tok", "Valid123!"));
    }

    @Test
    void resetPassword_shouldSuccess() {
        PasswordResetToken token = PasswordResetToken.builder().email("e").expiresAt(LocalDateTime.now().plusDays(1)).build();
        User user = new User();
        user.setEmail("e");

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("tok")).thenReturn(Optional.of(token));
        when(userRepository.findByEmail("e")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("enc");

        authService.resetPassword("tok", "Valid123!");

        verify(userRepository).save(user);
        assertTrue(token.isUsed());
    }
}
