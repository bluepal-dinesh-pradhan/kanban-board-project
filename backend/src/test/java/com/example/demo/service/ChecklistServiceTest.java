package com.example.demo.service;

import com.example.demo.dto.ChecklistDto;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChecklistServiceTest {

    @Autowired
    private ChecklistService checklistService;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @MockBean
    private WebSocketNotificationService webSocketNotificationService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private ActivityService activityService;

    private User user;
    private Card card;
    private Board board;

    @BeforeEach
    void setUp() {
        checklistRepository.deleteAll();
        cardRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder().email("ch@t.com").password("p").fullName("CH").build();
        user = userRepository.save(user);

        board = Board.builder().title("B").owner(user).build();
        board = boardRepository.save(board);

        BoardMember member = BoardMember.builder().board(board).user(user).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(member);

        BoardColumn column = BoardColumn.builder().board(board).title("Col").position(0).build();
        column = boardColumnRepository.save(column);

        card = Card.builder().column(column).title("Card").position(0).build();
        card = cardRepository.save(card);
    }

    @Test
    void addItem() {
        ChecklistDto result = checklistService.addItem(card.getId(), "Task 1", user.getId());

        assertNotNull(result);
        assertEquals("Task 1", result.getTitle());
    }

    @Test
    void toggleItem() {
        Checklist item = Checklist.builder().card(card).title("T").position(0).build();
        item = checklistRepository.save(item);

        ChecklistDto result = checklistService.toggleItem(item.getId(), user.getId());

        assertTrue(result.isCompleted());
    }

    @Test
    void deleteItem() {
        Checklist item = Checklist.builder().card(card).title("D").position(0).build();
        item = checklistRepository.save(item);

        checklistService.deleteItem(item.getId(), user.getId());

        assertFalse(checklistRepository.existsById(item.getId()));
    }

    @Test
    void getProgress() {
        Checklist i1 = Checklist.builder().card(card).title("T1").position(0).completed(true).build();
        checklistRepository.save(i1);
        Checklist i2 = Checklist.builder().card(card).title("T2").position(1).completed(false).build();
        checklistRepository.save(i2);

        int[] result = checklistService.getProgress(card.getId());

        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
    }
}
