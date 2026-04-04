package com.example.demo.dto;

import com.example.demo.entity.BoardMember;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DtoTests {

    @Test
    void testEveryDto() {
        // ActivityDto - @Data @AllArgsConstructor @NoArgsConstructor
        ActivityDto activityDto = new ActivityDto();
        activityDto.setId(1L);
        activityDto.setAction("action");
        activityDto.setEntityType("type");
        activityDto.setEntityId(2L);
        activityDto.setUser(new UserDto());
        activityDto.setCreatedAt(LocalDateTime.now());
        assertNotNull(activityDto.toString());
        assertEquals(1L, activityDto.getId());

        // ApiResponse - @Data @AllArgsConstructor
        ApiResponse<String> apiResponse = ApiResponse.ok("msg", "data");
        assertTrue(apiResponse.isSuccess());
        apiResponse = ApiResponse.error("error");
        assertFalse(apiResponse.isSuccess());
        assertNotNull(apiResponse.toString());

        // AttachmentDto - @Data @AllArgsConstructor @NoArgsConstructor
        AttachmentDto attachmentDto = new AttachmentDto();
        attachmentDto.setId(1L);
        attachmentDto.setFileName("file");
        attachmentDto.setFileType("type");
        attachmentDto.setFileSize(100L);
        attachmentDto.setUploadedByName("user");
        attachmentDto.setUploadedById(1L);
        attachmentDto.setUploadedAt(LocalDateTime.now());
        attachmentDto.setDownloadUrl("url");
        assertNotNull(attachmentDto.toString());

        // AuthRequest - @Data (NoArgsConstructor only by default)
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("email@ex.com");
        authRequest.setPassword("password");
        assertEquals("email@ex.com", authRequest.getEmail());
        assertNotNull(authRequest.toString());

        // AuthResponse - @Data @AllArgsConstructor
        AuthResponse authResponse = new AuthResponse("access", "refresh", new UserDto());
        assertEquals("access", authResponse.getAccessToken());
        assertNotNull(authResponse.toString());

        // BoardDto - @Data @AllArgsConstructor @NoArgsConstructor
        BoardDto boardDto = new BoardDto();
        boardDto.setId(1L);
        boardDto.setTitle("title");
        boardDto.setOwner(new UserDto());
        boardDto.setRole("role");
        boardDto.setBackground("bg");
        boardDto.setMemberCount(10);
        boardDto.setCreatedAt(LocalDateTime.now());
        assertEquals(1L, boardDto.getId());
        assertNotNull(boardDto.toString());

        // BoardEventDto - @Data @Builder @NoArgsConstructor @AllArgsConstructor
        BoardEventDto eventDto = BoardEventDto.builder()
                .eventType("type")
                .boardId(1L)
                .userId(1L)
                .userName("user")
                .payload("data")
                .timestamp(LocalDateTime.now())
                .build();
        assertEquals("type", eventDto.getEventType());
        assertNotNull(eventDto.toString());

        // BoardMemberDto - @Data @AllArgsConstructor @NoArgsConstructor
        BoardMemberDto memberDto = new BoardMemberDto();
        memberDto.setId(1L);
        memberDto.setUser(new UserDto());
        memberDto.setRole("ADMIN");
        assertEquals("ADMIN", memberDto.getRole());
        assertNotNull(memberDto.toString());

        // BoardMembersDto - @Data @NoArgsConstructor @AllArgsConstructor
        BoardMembersDto membersDto = new BoardMembersDto(Collections.emptyList(), Collections.emptyList());
        assertNotNull(membersDto.getMembers());
        assertNotNull(membersDto.toString());

        // BoardRequest - @Data
        BoardRequest boardRequest = new BoardRequest();
        boardRequest.setTitle("title");
        boardRequest.setBackground("bg");
        assertNotNull(boardRequest.toString());

        // BoardUpdateRequest - @Data
        BoardUpdateRequest boardUpdate = new BoardUpdateRequest();
        boardUpdate.setTitle("t");
        boardUpdate.setBackground("b");
        assertNotNull(boardUpdate.toString());

        // CardDto - @Data @AllArgsConstructor @NoArgsConstructor
        CardDto cardDto = new CardDto();
        cardDto.setId(1L);
        cardDto.setTitle("t");
        cardDto.setDescription("d");
        cardDto.setDueDate(LocalDate.now());
        cardDto.setPosition(0);
        cardDto.setColumnId(1L);
        cardDto.setLabels(Collections.emptyList());
        cardDto.setReminders(Collections.emptyList());
        cardDto.setCommentCount(0);
        cardDto.setCreatedAt(LocalDateTime.now());
        cardDto.setOverdue(false);
        cardDto.setPriority("HIGH");
        cardDto.setAssigneeId(1L);
        cardDto.setAssigneeName("name");
        cardDto.setAssigneeEmail("email");
        cardDto.setChecklistTotal(0);
        cardDto.setChecklistCompleted(0);
        cardDto.setAttachmentCount(0);
        assertNotNull(cardDto.toString());
        assertFalse(cardDto.isOverdue());

        // CardReminderDto - @Data @AllArgsConstructor @NoArgsConstructor
        CardReminderDto reminderDto = new CardReminderDto();
        reminderDto.setId(1L);
        reminderDto.setReminderType("type");
        reminderDto.setReminderDateTime(LocalDateTime.now());
        reminderDto.setTriggered(true);
        assertTrue(reminderDto.isTriggered());
        assertNotNull(reminderDto.toString());

        // CardRequest - @Data
        CardRequest cardRequest = new CardRequest();
        cardRequest.setTitle("t");
        cardRequest.setDescription("d");
        cardRequest.setColumnId(1L);
        cardRequest.setPriority("P");
        cardRequest.setDueDate(LocalDate.now());
        cardRequest.setLabels(Collections.emptyList());
        cardRequest.setReminderType("type");
        assertNotNull(cardRequest.toString());

        // ChangePasswordRequest - @Data @NoArgsConstructor @AllArgsConstructor
        ChangePasswordRequest cpReq = new ChangePasswordRequest("old", "new");
        assertEquals("old", cpReq.getCurrentPassword());
        assertNotNull(cpReq.toString());

        // ChecklistDto - @Data @AllArgsConstructor @NoArgsConstructor
        ChecklistDto checklistDto = new ChecklistDto();
        checklistDto.setId(1L);
        checklistDto.setTitle("t");
        checklistDto.setCompleted(true);
        checklistDto.setPosition(0);
        checklistDto.setCreatedAt(LocalDateTime.now());
        assertNotNull(checklistDto.toString());

        // ColumnDto - @Data @AllArgsConstructor @NoArgsConstructor
        ColumnDto columnDto = new ColumnDto();
        columnDto.setId(1L);
        columnDto.setTitle("t");
        columnDto.setPosition(0);
        columnDto.setCards(Collections.emptyList());
        assertNotNull(columnDto.toString());

        // ColumnRequest - @Data
        ColumnRequest colReq = new ColumnRequest();
        colReq.setTitle("t");
        assertEquals("t", colReq.getTitle());
        assertNotNull(colReq.toString());

        // ColumnUpdateRequest - @Data
        ColumnUpdateRequest colUpd = new ColumnUpdateRequest();
        colUpd.setTitle("t");
        assertEquals("t", colUpd.getTitle());
        assertNotNull(colUpd.toString());

        // CommentDto - @Data @AllArgsConstructor @NoArgsConstructor
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setContent("c");
        commentDto.setAuthor(new UserDto());
        commentDto.setCreatedAt(LocalDateTime.now());
        assertNotNull(commentDto.toString());

        // CommentRequest - @Data
        CommentRequest commReq = new CommentRequest();
        commReq.setContent("c");
        assertEquals("c", commReq.getContent());
        assertNotNull(commReq.toString());

        // ForgotPasswordRequest - @Data
        ForgotPasswordRequest fpReq = new ForgotPasswordRequest();
        fpReq.setEmail("e");
        assertEquals("e", fpReq.getEmail());
        assertNotNull(fpReq.toString());

        // InvitationAcceptResponse - @Data @NoArgsConstructor @AllArgsConstructor
        InvitationAcceptResponse iaRes = new InvitationAcceptResponse(1L);
        assertEquals(1L, iaRes.getBoardId());
        assertNotNull(iaRes.toString());

        // InvitationDetailsDto - @Data @NoArgsConstructor @AllArgsConstructor
        InvitationDetailsDto idDto = new InvitationDetailsDto();
        idDto.setEmail("e");
        idDto.setBoardId(1L);
        idDto.setBoardTitle("t");
        idDto.setRole("r");
        idDto.setStatus("s");
        idDto.setExpiresAt(LocalDateTime.now());
        idDto.setUserExists(true);
        assertTrue(idDto.isUserExists());
        assertNotNull(idDto.toString());

        // InvitationDto - @Data @AllArgsConstructor @NoArgsConstructor
        InvitationDto iDto = new InvitationDto();
        iDto.setId(1L);
        iDto.setEmail("e");
        iDto.setRole("r");
        iDto.setStatus("s");
        iDto.setInvitedBy(new UserDto());
        iDto.setCreatedAt(LocalDateTime.now());
        assertNotNull(iDto.toString());

        // InviteRequest - @Data
        InviteRequest invReq = new InviteRequest();
        invReq.setEmail("e");
        invReq.setRole(BoardMember.Role.EDITOR);
        assertEquals("e", invReq.getEmail());
        assertNotNull(invReq.toString());

        // InviteResponse - @Data @Builder @NoArgsConstructor @AllArgsConstructor
        InviteResponse invRes = InviteResponse.builder()
                .status("status")
                .message("m")
                .emailSent(true)
                .build();
        assertTrue(invRes.isEmailSent());
        assertNotNull(invRes.toString());

        // LabelDto - @Data @AllArgsConstructor @NoArgsConstructor
        LabelDto lDto = new LabelDto(1L, "c", "t");
        assertEquals(1L, lDto.getId());
        assertNotNull(lDto.toString());

        // MoveCardRequest - @Data
        MoveCardRequest mcReq = new MoveCardRequest();
        mcReq.setTargetColumnId(1L);
        mcReq.setNewPosition(5);
        assertEquals(1L, mcReq.getTargetColumnId());
        assertNotNull(mcReq.toString());

        // MoveColumnRequest - @Data @AllArgsConstructor @NoArgsConstructor
        MoveColumnRequest mcolReq = new MoveColumnRequest(5);
        assertEquals(5, mcolReq.getNewPosition());
        assertNotNull(mcolReq.toString());

        // NotificationDto - @Data @AllArgsConstructor @NoArgsConstructor
        NotificationDto nDto = new NotificationDto();
        nDto.setId(1L);
        nDto.setTitle("t");
        nDto.setMessage("m");
        nDto.setType("t");
        nDto.setRead(true);
        nDto.setCreatedAt(LocalDateTime.now());
        nDto.setCardId(1L);
        nDto.setCardTitle("t");
        nDto.setBoardId(1L);
        nDto.setBoardTitle("t");
        assertTrue(nDto.isRead());
        assertNotNull(nDto.toString());

        // NotificationPreferencesRequest - @Data @NoArgsConstructor @AllArgsConstructor @Builder
        NotificationPreferencesRequest npReq = NotificationPreferencesRequest.builder()
                .emailNotifications(true)
                .dueDateReminders(true)
                .boardInvitationEmails(true)
                .build();
        assertTrue(npReq.isEmailNotifications());
        assertNotNull(npReq.toString());

        // PageResponse - Custom NoArgs + AllArgs. NO SETTERS.
        PageResponse<String> pRes = new PageResponse<>(Collections.emptyList(), 0, 10, 0, 0, true, true);
        assertEquals(10, pRes.getSize());
        assertNotNull(pRes.toString());

        // RefreshTokenRequest - @Data
        RefreshTokenRequest rtReq = new RefreshTokenRequest();
        rtReq.setRefreshToken("t");
        assertEquals("t", rtReq.getRefreshToken());
        assertNotNull(rtReq.toString());

        // RegisterRequest - @Data
        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail("e");
        regReq.setPassword("p");
        regReq.setFullName("f");
        assertEquals("e", regReq.getEmail());
        assertNotNull(regReq.toString());

        // ResetPasswordRequest - @Data
        ResetPasswordRequest resReq = new ResetPasswordRequest();
        resReq.setToken("t");
        resReq.setNewPassword("p");
        assertEquals("t", resReq.getToken());
        assertNotNull(resReq.toString());

        // UserDto - @Data @AllArgsConstructor @NoArgsConstructor
        UserDto uDto = new UserDto(1L, "e", "n");
        assertEquals(1L, uDto.getId());
        assertNotNull(uDto.toString());

        // UserProfileResponse - @Data @NoArgsConstructor @AllArgsConstructor @Builder
        UserProfileResponse upRes = UserProfileResponse.builder()
                .id(1L)
                .fullName("n")
                .email("e")
                .emailNotifications(true)
                .dueDateReminders(true)
                .boardInvitationEmails(true)
                .build();
        assertEquals(1L, upRes.getId());
        assertNotNull(upRes.toString());
    }
}
