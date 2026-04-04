package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReminderServiceTest {

    @Autowired
    private ReminderService reminderService;

    @MockBean
    private CardReminderRepository cardReminderRepository;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private BoardMemberRepository boardMemberRepository;

    private Card card;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(123L).email("user@test.com").fullName("User").build();
        Board board = Board.builder().id(456L).title("Board").build();
        BoardColumn column = BoardColumn.builder().id(789L).board(board).build();
        card = Card.builder().id(1L).title("Card").dueDate(LocalDate.now().plusDays(1)).column(column).build();
        
        BoardMember member = BoardMember.builder().user(user).board(board).build();
        when(boardMemberRepository.findByBoardId(456L)).thenReturn(List.of(member));
    }

    @Test
    void createReminder_shouldSuccess() {
        reminderService.createReminder(card, CardReminder.ReminderType.ONE_DAY_BEFORE);

        verify(cardReminderRepository).deleteByCardId(card.getId());
        verify(cardReminderRepository).save(any(CardReminder.class));
    }

    @Test
    void processReminders_shouldSendNotifications() {
        CardReminder reminder = CardReminder.builder()
                .id(1L)
                .card(card)
                .user(user)
                .reminderType(CardReminder.ReminderType.ONE_DAY_BEFORE)
                .reminderDateTime(LocalDateTime.now().minusMinutes(1))
                .build();

        when(cardReminderRepository.findDueReminders(any(LocalDateTime.class))).thenReturn(List.of(reminder));

        reminderService.processReminders();

        verify(notificationService, atLeastOnce()).createDueDateReminder(eq(user.getId()), eq(card.getId()), anyString());
        verify(emailService, atLeastOnce()).sendDueDateReminder(user.getEmail(), user.getFullName(), card.getTitle(), "Board", card.getDueDate());
        assertTrue(reminder.isTriggered());
        verify(cardReminderRepository, atLeastOnce()).save(reminder);
    }

    @Test
    void deleteCardReminders_shouldSuccess() {
        reminderService.deleteCardReminders(1L);
        verify(cardReminderRepository).deleteByCardId(1L);
    }
}
