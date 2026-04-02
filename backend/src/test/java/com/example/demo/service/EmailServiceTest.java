package com.example.demo.service;

import com.example.demo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://test.com");
    }

    @Test
    void sendBoardInvitation_whenEnabled_shouldSend() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailService.sendBoardInvitation("test@email.com", "Inviter", "Board", "READER", true);

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendBoardInvitation_whenDisabled_shouldNotSend() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        boolean result = emailService.sendBoardInvitation("test@email.com", "Inviter", "Board", "READER", true);

        assertFalse(result);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_shouldSend() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailService.sendPasswordResetEmail("test@email.com", "token123");

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendDueDateReminder_shouldSend() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailService.sendDueDateReminder("test@email.com", "User", "Card", "Board", LocalDate.now());

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendBoardInvitation_withMailSenderNull_shouldReturnFalse() {
        ReflectionTestUtils.setField(emailService, "mailSender", null);
        
        boolean result = emailService.sendBoardInvitation("test@email.com", "Inviter", "Board", "READER", true);
        
        assertFalse(result);
        
        // Restore mailSender for other tests
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
    }
}
