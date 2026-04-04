package com.example.demo.service;

import com.example.demo.dto.NotificationDto;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createDueDateReminder_shouldSuccess() {
        User user = new User();
        user.setId(1L);
        Card card = new Card();
        card.setId(2L);
        BoardColumn column = new BoardColumn();
        Board board = new Board();
        board.setId(3L);
        board.setTitle("B");
        column.setBoard(board);
        card.setColumn(column);
        card.setTitle("T");

        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .card(card)
                .title("Due Date Reminder")
                .message("Msg")
                .type(Notification.NotificationType.DUE_DATE_REMINDER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card));
        when(notificationRepository.save(any())).thenReturn(notification);
        when(notificationRepository.countUnreadByUserId(1L)).thenReturn(5L);

        notificationService.createDueDateReminder(1L, 2L, "Msg");

        verify(notificationRepository).save(any());
        verify(webSocketNotificationService).sendNotification(eq(1L), any());
        verify(webSocketNotificationService).sendUnreadCount(1L, 5L);
    }

    @Test
    void createDueDateReminder_userNotFound_shouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> notificationService.createDueDateReminder(1L, 2L, "Msg"));
    }

    @Test
    void getUserNotifications_shouldReturnList() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        List<NotificationDto> result = notificationService.getUserNotifications(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserNotifications_paged_shouldReturnPage() {
        Page<Notification> page = new PageImpl<>(Collections.emptyList());
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class))).thenReturn(page);
        PageResponse<NotificationDto> result = notificationService.getUserNotifications(1L, 0, 10);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getUnreadCount_shouldReturnCount() {
        when(notificationRepository.countUnreadByUserId(1L)).thenReturn(10L);
        assertEquals(10L, notificationService.getUnreadCount(1L));
    }

    @Test
    void markAsRead_shouldSuccess() {
        User user = new User();
        user.setId(1L);
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.countUnreadByUserId(1L)).thenReturn(0L);

        notificationService.markAsRead(1L, 1L);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
        verify(webSocketNotificationService).sendUnreadCount(1L, 0L);
    }

    @Test
    void markAsRead_notFound_shouldThrow() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(1L, 1L));
    }

    @Test
    void markAsRead_wrongUser_shouldThrow() {
        User user = new User();
        user.setId(2L);
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        assertThrows(BadRequestException.class, () -> notificationService.markAsRead(1L, 1L));
    }

    @Test
    void markAllAsRead_shouldSuccess() {
        Notification n = new Notification();
        n.setRead(false);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n));

        notificationService.markAllAsRead(1L);

        assertTrue(n.isRead());
        verify(notificationRepository).saveAll(any());
        verify(webSocketNotificationService).sendUnreadCount(1L, 0L);
    }
}
