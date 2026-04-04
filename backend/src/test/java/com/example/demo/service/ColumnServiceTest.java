package com.example.demo.service;

import com.example.demo.dto.ColumnDto;
import com.example.demo.dto.ColumnRequest;
import com.example.demo.dto.ColumnUpdateRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ColumnServiceTest {

    @Autowired
    private ColumnService columnService;

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
    private WebSocketNotificationService webSocketNotificationService;

    private User user;
    private Board board;

    @BeforeEach
    void setUp() {
        columnRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder().email("user@test.com").password("pass").fullName("User").build();
        user = userRepository.saveAndFlush(user);

        board = Board.builder().title("Board").owner(user).build();
        board = boardRepository.saveAndFlush(board);
    }

    @Test
    void create_shouldSuccess() {
        ColumnRequest req = new ColumnRequest();
        req.setTitle("Column 1");

        ColumnDto result = columnService.create(board.getId(), req, user.getId());

        assertNotNull(result);
        assertEquals("Column 1", result.getTitle());
        assertTrue(columnRepository.existsById(result.getId()));
        verify(boardService).checkPermission(eq(board.getId()), eq(user.getId()), any());
    }

    @Test
    void update_shouldSuccess() {
        BoardColumn col = BoardColumn.builder().board(board).title("Col 1").position(0).build();
        col = columnRepository.saveAndFlush(col);

        ColumnUpdateRequest req = new ColumnUpdateRequest();
        req.setTitle("Updated Title");

        ColumnDto result = columnService.update(col.getId(), req, user.getId());

        assertEquals("Updated Title", result.getTitle());
        verify(boardService).checkPermission(eq(board.getId()), eq(user.getId()), any());
    }

    @Test
    void delete_shouldSuccess() {
        BoardColumn col = BoardColumn.builder().board(board).title("Col 1").position(0).build();
        col = columnRepository.saveAndFlush(col);

        columnService.delete(col.getId(), user.getId());

        assertFalse(columnRepository.existsById(col.getId()));
        verify(boardService).checkPermission(eq(board.getId()), eq(user.getId()), any());
    }

    @Test
    void moveColumn_shouldUpdatePositions() {
        BoardColumn col1 = BoardColumn.builder().board(board).title("Col 1").position(0).build();
        BoardColumn col2 = BoardColumn.builder().board(board).title("Col 2").position(1).build();
        col1 = columnRepository.saveAndFlush(col1);
        col2 = columnRepository.saveAndFlush(col2);

        columnService.moveColumn(col1.getId(), 1, user.getId());

        assertEquals(1, columnRepository.findById(col1.getId()).get().getPosition());
        assertEquals(0, columnRepository.findById(col2.getId()).get().getPosition());
    }
}
