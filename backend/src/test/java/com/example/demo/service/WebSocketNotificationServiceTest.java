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

@SpringBootTest
@ActiveProfiles("test")
public class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketNotificationService webSocketNotificationService;

    @Test
    void sendNotification_shouldSuccess() {
        NotificationDto dto = new NotificationDto();
        webSocketNotificationService.sendNotification(1L, dto);
        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/notifications"), eq(dto));
    }

    @Test
    void sendNotification_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(Object.class));
        webSocketNotificationService.sendNotification(1L, new NotificationDto());
    }

    @Test
    void sendUnreadCount_shouldSuccess() {
        webSocketNotificationService.sendUnreadCount(1L, 5L);
        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/unread-count"), eq(5L));
    }

    @Test
    void sendUnreadCount_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(Object.class));
        webSocketNotificationService.sendUnreadCount(1L, 5L);
    }

    @Test
    void broadcastBoardEvent_shouldSuccess() {
        webSocketNotificationService.broadcastBoardEvent(1L, "type", 2L, "user", "payload");
        verify(messagingTemplate).convertAndSend(eq("/topic/board/1"), any(Object.class));
    }

    @Test
    void broadcastBoardEvent_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        webSocketNotificationService.broadcastBoardEvent(1L, "type", 2L, "user", "payload");
    }

    @Test
    void broadcastActivityUpdate_shouldSuccess() {
        ActivityDto dto = new ActivityDto();
        webSocketNotificationService.broadcastActivityUpdate(1L, dto);
        verify(messagingTemplate).convertAndSend(eq("/topic/board/1/activity"), any(Object.class));
    }

    @Test
    void broadcastActivityUpdate_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        webSocketNotificationService.broadcastActivityUpdate(1L, new ActivityDto());
    }

    @Test
    void broadcastBoardPresence_shouldSuccess() {
        webSocketNotificationService.broadcastBoardPresence(1L, "data");
        verify(messagingTemplate).convertAndSend(eq("/topic/board/1/presence"), any(Object.class));
    }

    @Test
    void broadcastBoardPresence_shouldCatchException() {
        doThrow(new RuntimeException("Error")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        webSocketNotificationService.broadcastBoardPresence(1L, "data");
    }
}
