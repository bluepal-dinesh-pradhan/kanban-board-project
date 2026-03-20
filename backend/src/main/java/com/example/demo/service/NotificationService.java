package com.example.demo.service;

import com.example.demo.dto.NotificationDto;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @Transactional
    public void createDueDateReminder(Long userId, Long cardId, String message) {
        log.info("Creating due date notification for user {} card {}", userId, cardId);
        log.debug("Creating due date notification entry for card {}", cardId);
        User user = userRepository.findById(userId).orElseThrow();
        Card card = cardRepository.findById(cardId).orElseThrow();
        
        Notification notification = Notification.builder()
                .user(user)
                .card(card)
                .title("Due Date Reminder")
                .message(message)
                .type(Notification.NotificationType.DUE_DATE_REMINDER)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Due date notification created successfully for user {} card {}", userId, cardId);

        // Send instant update via WebSocket
        webSocketNotificationService.sendNotification(userId, NotificationDto.from(savedNotification));
        long unreadCount = notificationRepository.countUnreadByUserId(userId);
        webSocketNotificationService.sendUnreadCount(userId, unreadCount);
    }

    public List<NotificationDto> getUserNotifications(Long userId) {
        log.info("Fetching notifications for user {}", userId);
        List<NotificationDto> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());
        log.debug("Fetching {} notifications for user {}", notifications.size(), userId);
        log.info("Notifications fetched successfully for user {}", userId);
        return notifications;
    }

    public PageResponse<NotificationDto> getUserNotifications(Long userId, int page, int size) {
        log.info("Fetching notifications for user {} with page {} and size {}", userId, page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        PageResponse<NotificationDto> response = PageResponse.from(notifications.map(NotificationDto::from));
        log.debug("Fetching {} notifications for user {} (page {}, size {})", response.getContent().size(), userId, page, size);
        log.info("Notifications fetched successfully for user {} with page {} and size {}", userId, page, size);
        return response;
    }

    public long getUnreadCount(Long userId) {
        log.info("Fetching unread notification count for user {}", userId);
        long count = notificationRepository.countUnreadByUserId(userId);
        log.debug("User {} has {} unread notifications", userId, count);
        log.info("Unread notification count fetched successfully for user {}", userId);
        return count;
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification {} as read for user {}", notificationId, userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to access notification {} without permission", userId, notificationId);
            throw new RuntimeException("Access denied");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marked as read for user {}", notificationId, userId);
        
        // Update unread count via WebSocket
        long unreadCount = notificationRepository.countUnreadByUserId(userId);
        webSocketNotificationService.sendUnreadCount(userId, unreadCount);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.debug("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
        log.info("All notifications marked as read for user {}", userId);

        // Push 0 unread count after marking all as read
        webSocketNotificationService.sendUnreadCount(userId, 0);
    }
}
