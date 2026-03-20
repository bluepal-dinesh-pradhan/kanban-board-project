package com.example.demo.service;

import com.example.demo.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void sendNotification(Long userId, NotificationDto notification) {
        log.info("WebSocket: Sending notification to user {}", userId);
        messagingTemplate.convertAndSend(
            "/topic/user/" + userId + "/notifications", 
            notification
        );
    }
    
    public void sendUnreadCount(Long userId, long count) {
        log.info("WebSocket: Sending unread count {} to user {}", count, userId);
        messagingTemplate.convertAndSend(
            "/topic/user/" + userId + "/unread-count", 
            count
        );
    }
}
