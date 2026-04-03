package com.example.demo.dto;

import com.example.demo.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoConversionTest {

    @Test
    void userDtoFrom_ShouldConvertCorrectly() {
        User user = User.builder()
                .id(1L)
                .email("test@ex.com")
                .fullName("Test User")
                .build();
        UserDto dto = UserDto.from(user);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getFullName(), dto.getFullName());
    }

    @Test
    void activityDtoFrom_ShouldConvertCorrectly() {
        User user = User.builder().id(1L).email("t@e.com").fullName("Name").build();
        Activity activity = Activity.builder()
                .id(1L)
                .action("Test Action")
                .entityType("BOARD")
                .entityId(10L)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
        ActivityDto dto = ActivityDto.from(activity);
        assertEquals(activity.getId(), dto.getId());
        assertEquals(activity.getAction(), dto.getAction());
        assertEquals(user.getFullName(), dto.getUser().getFullName());
    }

    @Test
    void attachmentDtoFrom_ShouldConvertCorrectly() {
        User user = User.builder().id(1L).fullName("Uploader").build();
        Attachment attachment = Attachment.builder()
                .id(1L)
                .fileName("test.txt")
                .fileType("text/plain")
                .fileSize(1024L)
                .uploadedBy(user)
                .uploadedAt(LocalDateTime.now())
                .build();
        AttachmentDto dto = AttachmentDto.from(attachment);
        assertEquals(attachment.getId(), dto.getId());
        assertEquals(user.getFullName(), dto.getUploadedByName());
        assertTrue(dto.getDownloadUrl().contains("/1/download"));
    }

    @Test
    void boardDtoFrom_ShouldConvertCorrectly() {
        User owner = User.builder().id(1L).fullName("Owner").build();
        Board board = Board.builder()
                .id(1L)
                .title("Board")
                .owner(owner)
                .background("blue")
                .createdAt(LocalDateTime.now())
                .members(Collections.emptyList())
                .build();
        BoardDto dto = BoardDto.from(board, "OWNER");
        assertEquals(board.getTitle(), dto.getTitle());
        assertEquals("OWNER", dto.getRole());
        assertEquals(0, dto.getMemberCount());
    }

    @Test
    void boardMemberDtoFrom_ShouldConvertCorrectly() {
        User user = User.builder().id(1L).fullName("User").build();
        BoardMember member = BoardMember.builder()
                .id(1L)
                .user(user)
                .role(BoardMember.Role.EDITOR)
                .build();
        BoardMemberDto dto = BoardMemberDto.from(member);
        assertEquals(user.getFullName(), dto.getUser().getFullName());
        assertEquals("EDITOR", dto.getRole());
    }

    @Test
    void cardDtoFrom_ShouldConvertCorrectly() {
        BoardColumn col = BoardColumn.builder().id(1L).build();
        User assignee = User.builder().id(2L).fullName("Assignee").email("a@b.c").build();
        Card card = Card.builder()
                .id(1L)
                .title("Card")
                .description("Desc")
                .column(col)
                .labels(Collections.emptyList())
                .reminders(Collections.emptyList())
                .comments(Collections.emptyList())
                .checklists(Collections.emptyList())
                .assignee(assignee)
                .priority(Priority.HIGH)
                .dueDate(LocalDate.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .build();
        CardDto dto = CardDto.from(card);
        assertEquals(card.getId(), dto.getId());
        assertEquals("HIGH", dto.getPriority());
        assertEquals(assignee.getFullName(), dto.getAssigneeName());
        assertTrue(dto.isOverdue());
    }

    @Test
    void cardReminderDtoFrom_ShouldConvertCorrectly() {
        CardReminder reminder = CardReminder.builder()
                .id(1L)
                .reminderDateTime(LocalDateTime.now())
                .reminderType(CardReminder.ReminderType.AT_DUE_TIME)
                .triggered(false)
                .build();
        CardReminderDto dto = CardReminderDto.from(reminder);
        assertEquals(reminder.getId(), dto.getId());
        assertEquals("AT_DUE_TIME", dto.getReminderType());
    }

    @Test
    void checklistDtoFrom_ShouldConvertCorrectly() {
        Checklist checklist = Checklist.builder()
                .id(1L)
                .title("Item")
                .completed(true)
                .build();
        ChecklistDto dto = ChecklistDto.from(checklist);
        assertEquals(checklist.getTitle(), dto.getTitle());
        assertTrue(dto.isCompleted());
    }

    @Test
    void columnDtoFrom_ShouldConvertCorrectly() {
        BoardColumn col = BoardColumn.builder()
                .id(1L)
                .title("Col")
                .position(0)
                .cards(Collections.emptyList())
                .build();
        ColumnDto dto = ColumnDto.from(col);
        assertEquals(col.getTitle(), dto.getTitle());
        assertEquals(0, dto.getCards().size());
    }

    @Test
    void commentDtoFrom_ShouldConvertCorrectly() {
        User user = User.builder().id(1L).fullName("User").build();
        Comment comment = Comment.builder()
                .id(1L)
                .content("Hi")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
        CommentDto dto = CommentDto.from(comment);
        assertEquals("Hi", dto.getContent());
        assertEquals(user.getFullName(), dto.getAuthor().getFullName());
    }

    @Test
    void invitationDtoFrom_ShouldConvertCorrectly() {
        User inviter = User.builder().id(1L).fullName("Inviter").build();
        Invitation invitation = Invitation.builder()
                .id(1L)
                .email("guest@ex.com")
                .invitedBy(inviter)
                .status(Invitation.InvitationStatus.PENDING)
                .role(BoardMember.Role.EDITOR)
                .createdAt(LocalDateTime.now())
                .build();
        InvitationDto dto = InvitationDto.from(invitation);
        assertEquals(invitation.getEmail(), dto.getEmail());
        assertEquals(inviter.getFullName(), dto.getInvitedBy().getFullName());
    }

    @Test
    void notificationDtoFrom_ShouldConvertCorrectly() {
        Board board = Board.builder().id(1L).title("Board").build();
        BoardColumn col = BoardColumn.builder().id(1L).title("Col").board(board).build();
        Card card = Card.builder().id(1L).title("Card").column(col).build();

        Notification notification = Notification.builder()
                .id(1L)
                .title("Title")
                .message("Msg")
                .type(Notification.NotificationType.DUE_DATE_REMINDER)
                .isRead(false)
                .card(card)
                .createdAt(LocalDateTime.now())
                .build();
        NotificationDto dto = NotificationDto.from(notification);
        assertEquals(notification.getMessage(), dto.getMessage());
        assertEquals("DUE_DATE_REMINDER", dto.getType());
        assertEquals(card.getId(), dto.getCardId());
        assertEquals(board.getId(), dto.getBoardId());
    }

    @Test
    void pageResponseFrom_ShouldConvertCorrectly() {
        List<String> content = List.of("A", "B");
        Page<String> page = new PageImpl<>(content);
        PageResponse<String> response = PageResponse.from(page);
        assertEquals(2, response.getContent().size());
        assertEquals(1, response.getTotalPages());
    }
}
