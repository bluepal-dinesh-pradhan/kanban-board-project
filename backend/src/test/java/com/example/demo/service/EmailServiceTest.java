package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void sendBoardInvitation_shouldSuccess_userExists() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailService.sendBoardInvitation("to@test.com", "Inviter", "Board", "EDITOR", true);

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendBoardInvitation_shouldSuccess_userNotExists() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailService.sendBoardInvitation("to@test.com", "Inviter", "Board", "EDITOR", false);

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendBoardInvitation_disabled_shouldReturnFalse() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);
        boolean result = emailService.sendBoardInvitation("to@test.com", "Inviter", "Board", "EDITOR", true);
        assertFalse(result);
    }

    @Test
    void sendBoardInvitation_exception_shouldReturnFalse() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail Error"));
        boolean result = emailService.sendBoardInvitation("to@test.com", "Inviter", "Board", "EDITOR", true);
        assertFalse(result);
    }

    @Test
    void sendDueDateReminder_shouldSuccess() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailService.sendDueDateReminder("to@test.com", "User", "Card", "Board", LocalDate.now());

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendDueDateReminder_exception_shouldReturnFalse() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail Error"));
        boolean result = emailService.sendDueDateReminder("to@test.com", "User", "Card", "Board", LocalDate.now());
        assertFalse(result);
    }

    @Test
    void sendPasswordResetEmail_shouldSuccess() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailService.sendPasswordResetEmail("to@test.com", "token");

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_exception_shouldReturnFalse() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail Error"));
        boolean result = emailService.sendPasswordResetEmail("to@test.com", "token");
        assertFalse(result);
    }
}
