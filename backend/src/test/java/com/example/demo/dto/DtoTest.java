package com.example.demo.dto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DtoTest {

    @Test
    void testAuthDtos() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@email.com");
        authRequest.setPassword("password");
        assertEquals("test@email.com", authRequest.getEmail());
        assertEquals("password", authRequest.getPassword());

        UserDto userDto = new UserDto(1L, "test@email.com", "Name");
        AuthResponse authResponse = new AuthResponse("access", "refresh", userDto);
        assertEquals("access", authResponse.getAccessToken());
        assertEquals("refresh", authResponse.getRefreshToken());
        assertEquals(userDto, authResponse.getUser());
        
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("token");
        assertEquals("token", refreshRequest.getRefreshToken());

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@email.com");
        registerRequest.setPassword("pass");
        registerRequest.setFullName("Full Name");
        assertEquals("test@email.com", registerRequest.getEmail());
        assertEquals("pass", registerRequest.getPassword());
        assertEquals("Full Name", registerRequest.getFullName());
    }

    @Test
    void testBoardDtos() {
        BoardRequest boardRequest = new BoardRequest();
        boardRequest.setTitle("Title");
        boardRequest.setBackground("Blue");
        assertEquals("Title", boardRequest.getTitle());
        assertEquals("Blue", boardRequest.getBackground());

        BoardUpdateRequest boardUpdateRequest = new BoardUpdateRequest();
        boardUpdateRequest.setTitle("New Title");
        boardUpdateRequest.setBackground("Green");
        assertEquals("New Title", boardUpdateRequest.getTitle());
        assertEquals("Green", boardUpdateRequest.getBackground());

        BoardDto boardDto = new BoardDto(1L, "Title", null, "ADMIN", "Bg", 5, LocalDateTime.now());
        assertEquals(1L, boardDto.getId());
        assertEquals("Title", boardDto.getTitle());

        BoardMemberDto memberDto = new BoardMemberDto();
        memberDto.setId(1L);
        UserDto userDtoM = new UserDto(2L, "test@email.com", "Name");
        memberDto.setUser(userDtoM);
        memberDto.setRole("OWNER");
        assertEquals("OWNER", memberDto.getRole());
        assertEquals(userDtoM, memberDto.getUser());

        BoardMembersDto membersDto = new BoardMembersDto(List.of(memberDto), Collections.emptyList());
        assertEquals(1, membersDto.getMembers().size());
    }

    @Test
    void testCardDtos() {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setTitle("Card");
        cardRequest.setDescription("Desc");
        cardRequest.setColumnId(1L);
        cardRequest.setPriority("HIGH");
        cardRequest.setDueDate(LocalDate.now());
        assertEquals("Card", cardRequest.getTitle());
        assertEquals("HIGH", cardRequest.getPriority());

        CardDto cardDto = new CardDto();
        cardDto.setId(1L);
        cardDto.setTitle("Card");
        cardDto.setDescription("Desc");
        cardDto.setPosition(0);
        cardDto.setColumnId(1L);
        cardDto.setOverdue(false);
        cardDto.setCommentCount(2);
        cardDto.setPriority("LOW");
        assertEquals("LOW", cardDto.getPriority());
        assertEquals(2, cardDto.getCommentCount());

        LabelDto labelDto = new LabelDto(1L, "red", "urgent");
        assertEquals("red", labelDto.getColor());
        assertEquals("urgent", labelDto.getText());

        CardReminderDto reminderDto = new CardReminderDto();
        reminderDto.setId(1L);
        reminderDto.setReminderDateTime(LocalDateTime.now());
        reminderDto.setTriggered(true);
        assertTrue(reminderDto.isTriggered());

        MoveCardRequest moveRequest = new MoveCardRequest();
        moveRequest.setTargetColumnId(2L);
        moveRequest.setNewPosition(5);
        assertEquals(2L, moveRequest.getTargetColumnId());
        assertEquals(5, moveRequest.getNewPosition());
    }

    @Test
    void testColumnDtos() {
        ColumnRequest columnRequest = new ColumnRequest();
        columnRequest.setTitle("Col");
        assertEquals("Col", columnRequest.getTitle());

        ColumnUpdateRequest columnUpdate = new ColumnUpdateRequest();
        columnUpdate.setTitle("Updated");
        assertEquals("Updated", columnUpdate.getTitle());

        ColumnDto columnDto = new ColumnDto();
        columnDto.setId(1L);
        columnDto.setTitle("Title");
        columnDto.setPosition(1);
        columnDto.setCards(Collections.emptyList());
        assertEquals("Title", columnDto.getTitle());
        assertEquals(1, columnDto.getPosition());

        MoveColumnRequest moveColumn = new MoveColumnRequest();
        moveColumn.setNewPosition(2);
        assertEquals(2, moveColumn.getNewPosition());
    }

    @Test
    void testUserDtos() {
        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .id(1L)
                .email("test@email.com")
                .fullName("Name")
                .emailNotifications(true)
                .dueDateReminders(false)
                .boardInvitationEmails(true)
                .build();
        assertEquals(1L, profileResponse.getId());
        assertTrue(profileResponse.isEmailNotifications());
        assertFalse(profileResponse.isDueDateReminders());

        ChangePasswordRequest changePass = new ChangePasswordRequest();
        changePass.setCurrentPassword("old");
        changePass.setNewPassword("new");
        assertEquals("old", changePass.getCurrentPassword());
        assertEquals("new", changePass.getNewPassword());

        NotificationPreferencesRequest notifPrefs = NotificationPreferencesRequest.builder()
                .emailNotifications(true)
                .build();
        assertTrue(notifPrefs.isEmailNotifications());
    }

    @Test
    void testOtherDtos() {
        ApiResponse<String> response = ApiResponse.ok("Message", "Data");
        assertTrue(response.isSuccess());
        assertEquals("Message", response.getMessage());
        assertEquals("Data", response.getData());

        ApiResponse<String> error = ApiResponse.error("Error");
        assertFalse(error.isSuccess());
        assertEquals("Error", error.getMessage());

        CommentRequest commentReq = new CommentRequest();
        commentReq.setContent("Comment");
        assertEquals("Comment", commentReq.getContent());

        ChecklistDto checklistDto = new ChecklistDto();
        checklistDto.setId(1L);
        checklistDto.setTitle("Task");
        checklistDto.setCompleted(true);
        assertTrue(checklistDto.isCompleted());

        InvitationAcceptResponse acceptResponse = new InvitationAcceptResponse(1L);
        assertEquals(1L, acceptResponse.getBoardId());

        InviteRequest inviteReq = new InviteRequest();
        inviteReq.setEmail("test@email.com");
        assertEquals("test@email.com", inviteReq.getEmail());

        InviteResponse inviteRes = InviteResponse.builder().message("Sent").emailSent(true).build();
        assertTrue(inviteRes.isEmailSent());

        PageResponse<String> pageRes = new PageResponse<>(List.of("A", "B"), 0, 10, 2, 1, false, false);
        assertEquals(2, pageRes.getTotalElements());
    }
}
