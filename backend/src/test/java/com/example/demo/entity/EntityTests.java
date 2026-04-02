package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EntityTests {

    @Test
    void testEveryEntity() {
        // Activity - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        Activity activity = Activity.builder()
                .id(1L)
                .board(new Board())
                .user(new User())
                .action("action")
                .entityType("type")
                .entityId(1L)
                .createdAt(LocalDateTime.now())
                .build();
        assertEquals(1L, activity.getId());
        activity.onCreate();

        // Attachment - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        Attachment attachment = Attachment.builder()
                .id(1L)
                .card(new Card())
                .fileName("file")
                .storedName("stored")
                .fileType("type")
                .fileSize(100L)
                .uploadedBy(new User())
                .uploadedAt(LocalDateTime.now())
                .build();
        assertEquals("file", attachment.getFileName());
        attachment.onCreate();

        // Board - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        Board board = Board.builder()
                .id(1L)
                .title("title")
                .owner(new User())
                .background("blue")
                .archived(false)
                .createdAt(LocalDateTime.now())
                .columns(Collections.emptyList())
                .members(Collections.emptyList())
                .activities(Collections.emptyList())
                .invitations(Collections.emptyList())
                .build();
        assertEquals("title", board.getTitle());
        board.onCreate();

        // BoardColumn - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        BoardColumn column = BoardColumn.builder()
                .id(1L)
                .board(new Board())
                .title("title")
                .position(0)
                .archived(false)
                .cards(Collections.emptyList())
                .build();
        assertEquals(1L, column.getId());

        // BoardMember - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        BoardMember member = BoardMember.builder()
                .id(1L)
                .board(new Board())
                .user(new User())
                .role(BoardMember.Role.OWNER)
                .build();
        assertEquals(BoardMember.Role.OWNER, member.getRole());

        // Card - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        Card card = Card.builder()
                .id(1L)
                .column(new BoardColumn())
                .title("title")
                .description("desc")
                .dueDate(LocalDate.now())
                .position(0)
                .archived(false)
                .priority(Priority.HIGH)
                .assignee(new User())
                .createdAt(LocalDateTime.now())
                .labels(Collections.emptyList())
                .comments(Collections.emptyList())
                .reminders(Collections.emptyList())
                .checklists(Collections.emptyList())
                .attachments(Collections.emptyList())
                .build();
        assertEquals(Priority.HIGH, card.getPriority());
        card.onCreate();

        // CardLabel - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        CardLabel label = CardLabel.builder()
                .id(1L)
                .card(new Card())
                .color("red")
                .text("urgent")
                .build();
        assertEquals("red", label.getColor());

        // CardReminder - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        CardReminder reminder = CardReminder.builder()
                .id(1L)
                .card(new Card())
                .user(new User())
                .reminderType(CardReminder.ReminderType.AT_DUE_TIME)
                .reminderDateTime(LocalDateTime.now())
                .triggered(true)
                .createdAt(LocalDateTime.now())
                .build();
        assertTrue(reminder.isTriggered());
        reminder.onCreate();
        assertEquals("At due time", CardReminder.ReminderType.AT_DUE_TIME.getDisplayName());

        // Checklist - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        Checklist checklist = Checklist.builder()
                .id(1L)
                .card(new Card())
                .title("check")
                .completed(true)
                .position(0)
                .createdAt(LocalDateTime.now())
                .build();
        assertTrue(checklist.isCompleted());
        checklist.onCreate();

        // Comment - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        Comment comment = Comment.builder()
                .id(1L)
                .card(new Card())
                .user(new User())
                .content("content")
                .createdAt(LocalDateTime.now())
                .build();
        assertEquals("content", comment.getContent());
        comment.onCreate();

        // Invitation - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        Invitation invitation = Invitation.builder()
                .id(1L)
                .board(new Board())
                .email("e")
                .token("t")
                .expiresAt(LocalDateTime.now())
                .role(BoardMember.Role.EDITOR)
                .status(Invitation.InvitationStatus.PENDING)
                .invitedBy(new User())
                .createdAt(LocalDateTime.now())
                .build();
        assertEquals(Invitation.InvitationStatus.PENDING, invitation.getStatus());
        invitation.onCreate();

        // Notification - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        Notification notification = Notification.builder()
                .id(1L)
                .user(new User())
                .card(new Card())
                .title("t")
                .message("m")
                .type(Notification.NotificationType.CARD_ASSIGNED)
                .isRead(true)
                .createdAt(LocalDateTime.now())
                .build();
        assertTrue(notification.isRead());
        notification.onCreate();

        // PasswordResetToken - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        PasswordResetToken prToken = PasswordResetToken.builder()
                .id(1L)
                .token("t")
                .email("e")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        assertFalse(prToken.isExpired());
        assertFalse(prToken.isUsed());

        // StarredBoard - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        StarredBoard starred = StarredBoard.builder()
                .id(1L)
                .user(new User())
                .board(new Board())
                .starredAt(LocalDateTime.now())
                .build();
        assertNotNull(starred.getStarredAt());
        starred.onCreate();

        // User - @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        User user = User.builder()
                .id(1L)
                .email("e")
                .password("p")
                .fullName("f")
                .emailNotifications(true)
                .dueDateReminders(true)
                .boardInvitationEmails(true)
                .createdAt(LocalDateTime.now())
                .ownedBoards(Collections.emptyList())
                .boardMemberships(Collections.emptyList())
                .build();
        assertEquals("e", user.getEmail());
        user.onCreate();
    }
}
