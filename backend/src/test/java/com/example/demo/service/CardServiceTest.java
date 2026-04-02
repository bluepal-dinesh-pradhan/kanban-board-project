package com.example.demo.service;

import com.example.demo.dto.CardDto;
import com.example.demo.dto.CardRequest;
import com.example.demo.dto.MoveCardRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

        CardDto response = cardService.create(board.getId(), req, user.getId());

        assertNotNull(response);
        assertEquals("Task 1", response.getTitle());
        assertEquals("HIGH", response.getPriority());
        assertTrue(cardRepository.existsById(response.getId()));
        verify(boardService).checkPermission(eq(board.getId()), eq(user.getId()), any());
    }

    @Test
    void updateCard_shouldSuccess() {
        Card card = Card.builder().column(column).title("Old").position(0).build();
        card = cardRepository.save(card);

        CardRequest req = new CardRequest();
        req.setTitle("Updated");
        req.setPriority("LOW");

        CardDto response = cardService.update(card.getId(), req, user.getId());

        assertEquals("Updated", response.getTitle());
        assertEquals("LOW", response.getPriority());
        verify(boardService).checkPermission(anyLong(), eq(user.getId()), any());
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
    void archiveCard_shouldSuccess() {
        Card card = Card.builder().column(column).title("Title").position(0).build();
        card = cardRepository.saveAndFlush(card);

        CardDto result = cardService.archive(card.getId(), user.getId());

        assertTrue(cardRepository.findById(card.getId()).get().isArchived());
        verify(boardService).checkPermission(eq(board.getId()), eq(user.getId()), any());
    }

    @Test
    void getCard_shouldReturnDto() {
        Card card = Card.builder().column(column).title("Title").position(0).build();
        card = cardRepository.saveAndFlush(card);

        CardDto result = cardService.getCard(card.getId(), user.getId());

        assertEquals("Title", result.getTitle());
        verify(boardService).checkAccess(eq(board.getId()), eq(user.getId()));
    }

    @Test
    void duplicate_shouldCreateCopy() {
        Card card = Card.builder().column(column).title("Original").position(0).build();
        card = cardRepository.saveAndFlush(card);

        CardDto result = cardService.duplicate(card.getId(), user.getId());

        assertEquals("Original (copy)", result.getTitle());
        assertEquals(2, cardRepository.countByColumnIdAndArchivedFalse(column.getId()));
        verify(boardService).checkPermission(eq(board.getId()), eq(user.getId()), any());
    }

    @Test
    void restore_shouldUnarchive() {
        Card card = Card.builder().column(column).title("Archived").archived(true).position(0).build();
        card = cardRepository.saveAndFlush(card);

        CardDto result = cardService.restore(card.getId(), user.getId());

        assertFalse(cardRepository.findById(card.getId()).get().isArchived());
        verify(boardService).checkPermission(eq(board.getId()), eq(user.getId()), any());
    }

    @Test
    void deleteCard_shouldRemoveFromRepo() {
        Card card = Card.builder().column(column).title("Card").position(0).build();
        card = cardRepository.save(card);

        cardService.delete(card.getId(), user.getId());

        assertFalse(cardRepository.existsById(card.getId()));
    }
}
