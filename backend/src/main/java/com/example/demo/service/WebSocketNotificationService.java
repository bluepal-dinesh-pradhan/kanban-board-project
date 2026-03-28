package com.example.demo.service;

import com.example.demo.dto.ActivityDto;
import com.example.demo.dto.BoardEventDto;
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

    // =====================================================
    // EXISTING METHODS (updated to use secure user queues)
    // =====================================================

    /**
     * Send notification to a specific user.
     * UPDATED: Uses /user/{userId}/queue/notifications (private queue)
     * instead of /topic/user/{userId}/notifications (public topic)
     */
    public void sendNotification(Long userId, NotificationDto notification) {
        log.info("WebSocket: Sending notification to user {}", userId);
        try {
            // Secure: only the target user receives this
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
        } catch (Exception e) {
            log.warn("WebSocket: Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send unread count to a specific user.
     * UPDATED: Uses private user queue
     */
    public void sendUnreadCount(Long userId, long count) {
        log.info("WebSocket: Sending unread count {} to user {}", count, userId);
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/unread-count",
                    count
            );
        } catch (Exception e) {
            log.warn("WebSocket: Failed to send unread count to user {}: {}", userId, e.getMessage());
        }
    }

    // =====================================================
    // NEW METHODS — Real-time board event broadcasting
    // =====================================================

    /**
     * Broadcast a board event to ALL users viewing this board.
     * Used for: card.created, card.updated, card.moved, card.deleted,
     *           column.created, column.updated, column.deleted,
     *           comment.added, comment.deleted, board.updated, member.added
     */
    public void broadcastBoardEvent(Long boardId, String eventType,
                                     Long userId, String userName, Object payload) {
        log.info("WebSocket: Broadcasting [{}] to board {} by user {} ({})",
                eventType, boardId, userName, userId);
        try {
            BoardEventDto event = BoardEventDto.of(eventType, boardId, userId, userName, payload);
            messagingTemplate.convertAndSend("/topic/board/" + boardId, event);
        } catch (Exception e) {
            log.warn("WebSocket: Failed to broadcast {} to board {}: {}",
                    eventType, boardId, e.getMessage());
        }
    }

    /**
     * Broadcast activity feed update to all users viewing this board.
     */
    public void broadcastActivityUpdate(Long boardId, ActivityDto activity) {
        log.info("WebSocket: Broadcasting activity update to board {}", boardId);
        try {
            messagingTemplate.convertAndSend(
                    "/topic/board/" + boardId + "/activity",
                    activity
            );
        } catch (Exception e) {
            log.warn("WebSocket: Failed to broadcast activity to board {}: {}",
                    boardId, e.getMessage());
        }
    }

    /**
     * Broadcast presence data (who is online on this board).
     */
    public void broadcastBoardPresence(Long boardId, Object presenceData) {
        log.info("WebSocket: Broadcasting presence update to board {}", boardId);
        try {
            messagingTemplate.convertAndSend(
                    "/topic/board/" + boardId + "/presence",
                    presenceData
            );
        } catch (Exception e) {
            log.warn("WebSocket: Failed to broadcast presence to board {}: {}",
                    boardId, e.getMessage());
        }
    }
}