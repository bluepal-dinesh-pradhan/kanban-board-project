package com.example.demo.service;

import com.example.demo.dto.NotificationDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    @Transactional
    public void createDueDateReminder(Long userId, Long cardId, String message) {
        User user = userRepository.findById(userId).orElseThrow();
        Card card = cardRepository.findById(cardId).orElseThrow();
        
        Notification notification = Notification.builder()
                .user(user)
                .card(card)
                .title("Due Date Reminder")
                .message(message)
                .type(Notification.NotificationType.DUE_DATE_REMINDER)
                .build();
        
        notificationRepository.save(notification);
    }

    public List<NotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
}