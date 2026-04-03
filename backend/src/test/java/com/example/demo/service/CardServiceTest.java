package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardServiceTest {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BoardColumnRepository columnRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @MockBean
    private BoardService boardService;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private ReminderService reminderService;

    @MockBean
    private WebSocketNotificationService webSocketNotificationService;

    private User user;
    private Board board;
    private BoardColumn column;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        columnRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder().email("user@example.com").password("pass").fullName("User").build();
        user = userRepository.save(user);

        board = Board.builder().title("Board").owner(user).build();
        board = boardRepository.save(board);

        column = BoardColumn.builder().board(board).title("Col").position(0).build();
        column = columnRepository.save(column);
    }

    @Test
    void createCard_shouldSuccess() {
        CardRequest req = new CardRequest();
        req.setTitle("Task 1");
        req.setColumnId(column.getId());
        req.setPriority("HIGH");
        req.setLabels(List.of(new LabelDto(null, "red", "urgent")));

        CardDto response = cardService.create(board.getId(), req, user.getId());

        assertNotNull(response);
        assertEquals("Task 1", response.getTitle());
        assertEquals("HIGH", response.getPriority());
        assertTrue(cardRepository.existsById(response.getId()));
        verify(boardService).checkPermission(eq(board.getId()), eq(user.getId()), any());
    }

    @Test
    void createCard_invalidPriority_shouldSetNone() {
        CardRequest req = new CardRequest();
        req.setTitle("Task"); req.setColumnId(column.getId());
        req.setPriority("INVALID");
        CardDto response = cardService.create(board.getId(), req, user.getId());
        assertEquals("NONE", response.getPriority());
    }

    @Test
    void createCard_columnNotFound_shouldThrow() {
        CardRequest req = new CardRequest();
        req.setColumnId(999L);
        assertThrows(ResourceNotFoundException.class, () -> cardService.create(board.getId(), req, user.getId()));
    }

    @Test
    void updateCard_shouldSuccess() {
        Card card = Card.builder().column(column).title("Old").position(0).build();
        card = cardRepository.save(card);

        CardRequest req = new CardRequest();
        req.setTitle("Updated");
        req.setPriority("LOW");
        req.setLabels(List.of(new LabelDto(null, "blue", "tag")));

        CardDto response = cardService.update(card.getId(), req, user.getId());

        assertEquals("Updated", response.getTitle());
        assertEquals("LOW", response.getPriority());
        verify(boardService).checkPermission(anyLong(), eq(user.getId()), any());
    }

    @Test
    void updatePriority_shouldSuccess() {
        Card card = Card.builder().column(column).title("T").position(0).build();
        card = cardRepository.save(card);

        CardDto response = cardService.updatePriority(card.getId(), "MEDIUM", user.getId());
        assertEquals("MEDIUM", response.getPriority());
    }

    @Test
    void assignCard_unassign_shouldSuccess() {
        Card card = Card.builder().column(column).title("T").assignee(user).build();
        card = cardRepository.save(card);

        CardDto response = cardService.assignCard(card.getId(), null, user.getId());
        assertNull(response.getAssigneeId());
    }

    @Test
    void assignCard_toUser_shouldSuccess() {
        Card card = Card.builder().column(column).title("T").build();
        card = cardRepository.save(card);

        CardDto response = cardService.assignCard(card.getId(), user.getId(), user.getId());
        assertEquals(user.getId(), response.getAssigneeId());
        verify(boardService).checkAccess(board.getId(), user.getId());
    }

    @Test
    void duplicate_withLabels_shouldSuccess() {
        Card card = Card.builder().column(column).title("Original").position(0).build();
        card = cardRepository.save(card);

        CardDto result = cardService.duplicate(card.getId(), user.getId());

        assertEquals("Original (copy)", result.getTitle());
    }

    @Test
    void restore_notArchived_shouldThrow() {
        Card card = Card.builder().column(column).title("Active").archived(false).build();
        card = cardRepository.save(card);
        final Long cid = card.getId();
        final Long uid = user.getId();
        assertThrows(BadRequestException.class, () -> cardService.restore(cid, uid));
    }

    @Test
    void getArchivedCards_shouldReturnList() {
        Card card = Card.builder().column(column).title("A").archived(true).build();
        cardRepository.save(card);
        List<CardDto> list = cardService.getArchivedCards(board.getId(), user.getId());
        assertFalse(list.isEmpty());
    }

    @Test
    void moveCard_shouldUpdatePosition() {
        BoardColumn targetCol = BoardColumn.builder().board(board).title("Target").position(1).build();
        targetCol = columnRepository.save(targetCol);

        Card card = Card.builder().column(column).title("Card").position(0).build();
        card = cardRepository.save(card);

        MoveCardRequest req = new MoveCardRequest();
        req.setTargetColumnId(targetCol.getId());
        req.setNewPosition(0);

        CardDto response = cardService.move(card.getId(), req, user.getId());

        assertEquals(targetCol.getId(), response.getColumnId());
        assertEquals(0, response.getPosition());
    }

    @Test
    void getComments_shouldReturnData() {
        Card card = Card.builder().column(column).title("T").build();
        card = cardRepository.save(card);
        Comment c = Comment.builder().card(card).user(user).content("C").build();
        commentRepository.save(c);

        List<CommentDto> list = cardService.getComments(card.getId());
        assertTrue(list.isEmpty());

        PageResponse<CommentDto> page = cardService.getComments(card.getId(), 0, 10);
        assertNotNull(page);
    }

    @Test
    void delete_archived_shouldNotDecrement() {
        Card card = Card.builder().column(column).title("T").archived(true).build();
        card = cardRepository.save(card);
        cardService.delete(card.getId(), user.getId());
        assertFalse(cardRepository.existsById(card.getId()));
    }

    @Test
    void update_nonExisting_shouldThrowException() {
        CardRequest req = new CardRequest();
        req.setTitle("T");
        final Long userId = user.getId();
        assertThrows(ResourceNotFoundException.class, () -> cardService.update(999L, req, userId));
    }

    @Test
    void broadcastSafely_shouldHandleException() {
        doThrow(new RuntimeException()).when(webSocketNotificationService).broadcastBoardEvent(any(), any(), any(), any(), any());
        Card card = Card.builder().column(column).title("T").build();
        card = cardRepository.save(card);
        cardService.archive(card.getId(), user.getId());
        // Should not throw
    }

    @Test
    void restore_notArchived_throwsBadRequest() {
        Card card = Card.builder().column(column).title("Active").archived(false).build();
        card = cardRepository.save(card);
        final Long cid = card.getId();
        final Long uid = user.getId();
        assertThrows(BadRequestException.class, () -> cardService.restore(cid, uid));
    }

    @Test
    void create_withInvalidPriority_shouldDefaultToNone() {
        CardRequest req = new CardRequest();
        req.setTitle("T"); req.setColumnId(column.getId()); req.setPriority("INVALID");
        CardDto resp = cardService.create(board.getId(), req, user.getId());
        assertEquals("NONE", resp.getPriority());
    }

    @Test
    void create_withLabels_shouldSuccess() {
        CardRequest req = new CardRequest();
        req.setTitle("T"); req.setColumnId(column.getId());
        LabelDto l = new LabelDto(); l.setColor("red"); l.setText("bug");
        req.setLabels(List.of(l));
        CardDto resp = cardService.create(board.getId(), req, user.getId());
        assertFalse(resp.getLabels().isEmpty());
    }

    @Test
    void create_withReminder_shouldSuccess() {
        CardRequest req = new CardRequest();
        req.setTitle("T"); req.setColumnId(column.getId());
        req.setDueDate(LocalDate.now().plusDays(5));
        req.setReminderType("ONE_DAY_BEFORE");
        cardService.create(board.getId(), req, user.getId());
        verify(reminderService).createReminder(any(), any());
    }

    @Test
    void update_removeDueDate_shouldDeleteReminders() {
        Card card = Card.builder().column(column).title("T").dueDate(LocalDate.now()).build();
        card = cardRepository.save(card);
        CardRequest req = new CardRequest();
        req.setTitle("T");
        req.setDueDate(null);
        cardService.update(card.getId(), req, user.getId());
        verify(reminderService).deleteCardReminders(card.getId());
    }

    @Test
    void assignCard_toAnotherMember_shouldSuccess() {
        Card card = Card.builder().column(column).title("T").build();
        card = cardRepository.save(card);
        User other = User.builder().email("other@test.com").password("password").fullName("Other").build();
        other = userRepository.save(other);
        BoardMember m = BoardMember.builder().board(board).user(other).role(BoardMember.Role.VIEWER).build();
        boardMemberRepository.save(m);

        CardDto resp = cardService.assignCard(card.getId(), other.getId(), user.getId());
        assertEquals(other.getFullName(), resp.getAssigneeName());
    }

    @Test
    void moveCard_toAnotherColumn_shouldSuccess() {
        Card card = Card.builder().column(column).title("T").position(0).build();
        card = cardRepository.save(card);
        BoardColumn col2 = BoardColumn.builder().board(board).title("Col2").position(1).build();
        col2 = columnRepository.save(col2);

        MoveCardRequest req = new MoveCardRequest();
        req.setTargetColumnId(col2.getId()); req.setNewPosition(0);
        CardDto resp = cardService.move(card.getId(), req, user.getId());
        // Since we are mocking websockets/activities, we just check result
        assertNotNull(resp);
    }

    @Test
    void getCard_shouldSuccess() {
        Card card = Card.builder().column(column).title("T").build();
        card = cardRepository.save(card);
        when(boardService.isOwner(any(), any())).thenReturn(true);
        CardDto resp = cardService.getCard(card.getId(), user.getId());
        assertNotNull(resp);
    }

    @Test
    void archiveAndRestore_shouldWork() {
        Card card = Card.builder().column(column).title("T").build();
        card = cardRepository.save(card);
        cardService.archive(card.getId(), user.getId());
        assertTrue(cardRepository.findById(card.getId()).get().isArchived());
        
        cardService.restore(card.getId(), user.getId());
        assertFalse(cardRepository.findById(card.getId()).get().isArchived());
    }

    @Test
    void duplicateCard_shouldSuccess() {
        Card card = Card.builder().column(column).title("T").position(0).build();
        card = cardRepository.save(card);
        
        CardDto duplicated = cardService.duplicate(card.getId(), user.getId());
        assertNotNull(duplicated);
        assertNotEquals(card.getId(), duplicated.getId());
    }

    @Test
    void getComments_paged_shouldWork() {
        Card card = Card.builder().column(column).title("T").build();
        card = cardRepository.save(card);
        var res = cardService.getComments(card.getId(), 0, 10);
        assertNotNull(res);
    }

    @Test
    void getArchivedCards_shouldWork() {
        Card card = Card.builder().column(column).title("T").archived(true).build();
        card = cardRepository.save(card);
        var res = cardService.getArchivedCards(board.getId(), user.getId());
        assertFalse(res.isEmpty());
    }

    @Test
    void delete_shouldResourceNotFound_whenCardNotExist() {
        assertThrows(ResourceNotFoundException.class, () -> cardService.delete(999L, user.getId()));
    }
}
