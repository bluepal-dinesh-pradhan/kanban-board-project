package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.security.JwtService;
import com.example.demo.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
    }

    @Test
    void register_withValidData_shouldSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("Password123!");
        req.setFullName("Test User");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(), anyString())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("refresh");

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("test@example.com", response.getUser().getEmail());
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }

    @Test
    void register_withDuplicateEmail_shouldThrowException() {
        User user = User.builder()
                .email("test@example.com")
                .password("pass")
                .fullName("Existing User")
                .build();
        userRepository.save(user);

        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("Password123!");
        req.setFullName("Test User");

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_withInvalidEmailFormat_shouldThrowException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("invalid-email");
        req.setPassword("Password123!");
        req.setFullName("Test User");

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void register_withWeakPassword_shouldThrowException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("123");
        req.setFullName("Test User");

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void login_withValidCredentials_shouldSuccess() {
        User user = User.builder()
                .email("test@example.com")
                .password("encoded")
                .fullName("Test User")
                .build();
        userRepository.save(user);

        AuthRequest req = new AuthRequest();
        req.setEmail("test@example.com");
        req.setPassword("password");

        when(jwtService.generateAccessToken(any(), anyString())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("refresh");

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_withWrongPassword_shouldThrowException() {
        AuthRequest req = new AuthRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrong");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void refresh_withValidToken_shouldSuccess() {
        User user = User.builder()
                .email("test@example.com")
                .password("encoded")
                .fullName("Test User")
                .build();
        userRepository.save(user);

        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("valid-refresh");

        when(jwtService.isValid("valid-refresh")).thenReturn(true);
        when(jwtService.getEmail("valid-refresh")).thenReturn("test@example.com");
        when(jwtService.generateAccessToken(any(), anyString())).thenReturn("new-access");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("new-refresh");

        AuthResponse response = authService.refresh(req);

        assertNotNull(response);
        assertEquals("new-access", response.getAccessToken());
    }

    @Test
    void refresh_withInvalidToken_shouldThrowException() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("invalid-token");

        when(jwtService.isValid("invalid-token")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.refresh(req));
    }

    @Test
    void forgotPassword_withExistingEmail_shouldSendEmail() {
        User user = User.builder()
                .email("test@example.com")
                .password("encoded")
                .fullName("Test User")
                .build();
        userRepository.saveAndFlush(user);

        authService.forgotPassword("test@example.com");

        assertTrue(passwordResetTokenRepository.findAll().stream().anyMatch(t -> t.getEmail().equals("test@example.com")));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void forgotPassword_withNonExistingEmail_shouldNotSendEmail() {
        authService.forgotPassword("non-existing@example.com");

        assertFalse(passwordResetTokenRepository.findAll().stream().anyMatch(t -> t.getEmail().equals("non-existing@example.com")));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_withValidToken_shouldSuccess() {
        User user = User.builder()
                .email("test@example.com")
                .password("old-pass")
                .fullName("Test User")
                .build();
        userRepository.saveAndFlush(user);

        PasswordResetToken token = PasswordResetToken.builder()
                .token("valid-token")
                .email("test@example.com")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.saveAndFlush(token);

        when(passwordEncoder.encode("NewPassword123!")).thenReturn("new-encoded");

        authService.resetPassword("valid-token", "NewPassword123!");

        User updatedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertEquals("new-encoded", updatedUser.getPassword());
        
        PasswordResetToken usedToken = passwordResetTokenRepository.findByTokenAndUsedFalse("valid-token").isEmpty() 
            ? passwordResetTokenRepository.findAll().get(0) : null;
        assertNotNull(usedToken);
        assertTrue(usedToken.isUsed());
    }

    @Test
    void resetPassword_withExpiredToken_shouldThrowException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("expired-token")
                .email("test@example.com")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(token);

        assertThrows(BadRequestException.class, () -> authService.resetPassword("expired-token", "NewPassword123!"));
    }
}
