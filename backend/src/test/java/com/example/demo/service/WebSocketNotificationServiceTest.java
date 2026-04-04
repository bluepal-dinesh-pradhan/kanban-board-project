package com.example.demo.service;

import com.example.demo.dto.ActivityDto;
import com.example.demo.dto.NotificationDto;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketNotificationService webSocketNotificationService;

    @Test
    void sendNotification_shouldSuccess() {
        NotificationDto dto = new NotificationDto();
        assertDoesNotThrow(() -> webSocketNotificationService.sendNotification(1L, dto));
        verify(messagingTemplate).convertAndSendToUser("1", "/queue/notifications", dto);
    }

    @Test
    void sendNotification_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(Object.class));
        assertDoesNotThrow(() -> webSocketNotificationService.sendNotification(1L, new NotificationDto()));
    }

    @Test
    void sendUnreadCount_shouldSuccess() {
        assertDoesNotThrow(() -> webSocketNotificationService.sendUnreadCount(1L, 5L));
        verify(messagingTemplate).convertAndSendToUser("1", "/queue/unread-count", 5L);
    }

    @Test
    void sendUnreadCount_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(Object.class));
        assertDoesNotThrow(() -> webSocketNotificationService.sendUnreadCount(1L, 5L));
    }

    @Test
    void broadcastBoardEvent_shouldSuccess() {
        assertDoesNotThrow(() -> webSocketNotificationService.broadcastBoardEvent(1L, "type", 2L, "user", "payload"));
        verify(messagingTemplate).convertAndSend(eq("/topic/board/1"), any(Object.class));
    }

    @Test
    void broadcastBoardEvent_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        assertDoesNotThrow(() -> webSocketNotificationService.broadcastBoardEvent(1L, "type", 2L, "user", "payload"));
    }

    @Test
    void broadcastActivityUpdate_shouldSuccess() {
        ActivityDto dto = new ActivityDto();
        assertDoesNotThrow(() -> webSocketNotificationService.broadcastActivityUpdate(1L, dto));
        verify(messagingTemplate).convertAndSend(eq("/topic/board/1/activity"), any(Object.class));
    }

    @Test
    void broadcastActivityUpdate_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        assertDoesNotThrow(() -> webSocketNotificationService.broadcastActivityUpdate(1L, new ActivityDto()));
    }

    @Test
    void broadcastBoardPresence_shouldSuccess() {
        assertDoesNotThrow(() -> webSocketNotificationService.broadcastBoardPresence(1L, "data"));
        verify(messagingTemplate).convertAndSend(eq("/topic/board/1/presence"), any(Object.class));
    }

    @Test
    void broadcastBoardPresence_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        assertDoesNotThrow(() -> webSocketNotificationService.broadcastBoardPresence(1L, "data"));
    }
}
