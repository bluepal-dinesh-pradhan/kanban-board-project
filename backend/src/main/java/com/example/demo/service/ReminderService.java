package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final CardReminderRepository cardReminderRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final BoardMemberRepository boardMemberRepository;

    @Transactional
    public void createReminder(Card card, CardReminder.ReminderType reminderType) {
        log.info("Creating reminders for card {} with reminder type {}", card.getId(), reminderType);
        if (card.getDueDate() == null) {
            log.info("Skipping reminder creation for card {}: due date is null", card.getId());
            return; // No due date, no reminder needed
        }

        // Delete existing reminders for this card (all users)
        cardReminderRepository.deleteByCardId(card.getId());

        LocalDateTime reminderDateTime = calculateReminderDateTime(card.getDueDate(), reminderType);

        Long boardId = card.getColumn().getBoard().getId();
        List<BoardMember> members = boardMemberRepository.findByBoardId(boardId);
        log.info(
                "Creating reminders for card {} on board {}. Members={}, reminderType={}, reminderDateTime={}",
                card.getId(),
                boardId,
                members.size(),
                reminderType,
                reminderDateTime
        );

        for (BoardMember member : members) {
            User user = member.getUser();
            CardReminder reminder = CardReminder.builder()
                    .card(card)
                    .user(user)
                    .reminderType(reminderType)
                    .reminderDateTime(reminderDateTime)
                    .build();
            cardReminderRepository.save(reminder);
            log.info("Created reminder for card {} for user {} at {}", card.getId(), user.getId(), reminderDateTime);
        }
        log.info("Reminders created successfully for card {}", card.getId());
    }

    private LocalDateTime calculateReminderDateTime(LocalDate dueDate, CardReminder.ReminderType reminderType) {
        log.debug("Calculating reminder time for dueDate {} and type {}", dueDate, reminderType);
        LocalTime reminderTime = LocalTime.of(9, 0); // 9 AM default
        
        LocalDateTime reminderDateTime = switch (reminderType) {
            case AT_DUE_TIME -> dueDate.atTime(reminderTime);
            case ONE_DAY_BEFORE -> dueDate.minusDays(1).atTime(reminderTime);
            case TWO_DAYS_BEFORE -> dueDate.minusDays(2).atTime(reminderTime);
            case ONE_WEEK_BEFORE -> dueDate.minusWeeks(1).atTime(reminderTime);
        };
        log.debug("Calculated reminder time {}", reminderDateTime);
        return reminderDateTime;
    }

    @Scheduled(fixedRate = 60000) // Run every 1 minute (60000 ms)
    @Transactional
    public void processReminders() {
        log.info("Processing reminders");
        LocalDateTime now = LocalDateTime.now();
        List<CardReminder> dueReminders = cardReminderRepository.findDueReminders(now);

        log.info("Processing {} due reminders at {}", dueReminders.size(), now);

        for (CardReminder reminder : dueReminders) {
            try {
                log.info(
                        "Triggering reminder {} for card {} user {} (type={}, dueDate={}, reminderAt={})",
                        reminder.getId(),
                        reminder.getCard().getId(),
                        reminder.getUser().getId(),
                        reminder.getReminderType(),
                        reminder.getCard().getDueDate(),
                        reminder.getReminderDateTime()
                );
                sendReminderNotifications(reminder);
                reminder.setTriggered(true);
                cardReminderRepository.save(reminder);
                log.info("Processed reminder {} for card {}", reminder.getId(), reminder.getCard().getId());
            } catch (Exception e) {
                log.error("Failed to process reminder {}: {}", reminder.getId(), e.getMessage(), e);
            }
        }
        log.info("Reminder processing completed");
    }

    private void sendReminderNotifications(CardReminder reminder) {
        log.info("Sending reminder notifications for reminder {}", reminder.getId());
        Card card = reminder.getCard();
        User user = reminder.getUser();
        String boardTitle = card.getColumn().getBoard().getTitle();
        
        String message = String.format("Card \"%s\" in board \"%s\" is due on %s", 
                card.getTitle(), boardTitle, card.getDueDate().toString());

        // 1. In-app notification
        log.info("Creating in-app due date notification for user {} card {}", user.getId(), card.getId());
        notificationService.createDueDateReminder(user.getId(), card.getId(), message);

        // 2. Email notification
        log.info("Sending due date email for user {} card {}", user.getId(), card.getId());
        sendEmailReminder(user, card, boardTitle);

        // Note: Browser push notifications will be handled by the frontend
        log.info("Sent reminder notifications for card {} to user {}", card.getId(), user.getId());
    }

    private void sendEmailReminder(User user, Card card, String boardTitle) {
        log.info("Sending email reminder to user {} for card {}", user.getId(), card.getId());
        try {
            emailService.sendDueDateReminder(
                user.getEmail(),
                user.getFullName(),
                card.getTitle(),
                boardTitle,
                card.getDueDate()
            );
            log.info("Email reminder sent successfully to user {} for card {}", user.getId(), card.getId());
        } catch (Exception e) {
            log.error("Failed to send email reminder to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Transactional
    public void deleteCardReminders(Long cardId) {
        log.info("Deleting reminders for card {}", cardId);
        cardReminderRepository.deleteByCardId(cardId);
        log.info("Reminders deleted for card {}", cardId);
    }
}
