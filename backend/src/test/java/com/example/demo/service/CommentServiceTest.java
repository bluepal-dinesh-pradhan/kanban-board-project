package com.example.demo.service;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.CommentRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

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
        commentRepository.deleteAll();
        cardRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder().email("c@t.com").password("p").fullName("C").build();
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
    void addComment() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test Comment");

        CommentDto result = commentService.addComment(card.getId(), request, user.getId());

        assertNotNull(result);
        assertEquals("Test Comment", result.getContent());
    }

    @Test
    void getComments() {
        Comment comment = Comment.builder().card(card).user(user).content("C").build();
        comment = commentRepository.saveAndFlush(comment);

        // Required to update the card's comments list for the test context
        card.getComments().add(comment);

        List<CommentDto> results = commentService.getComments(card.getId());

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void deleteComment() {
        Comment comment = Comment.builder().card(card).user(user).content("Delete me").build();
        comment = commentRepository.saveAndFlush(comment);

        // boardId, cardId, commentId, userId
        commentService.deleteComment(board.getId(), card.getId(), comment.getId(), user.getId());

        assertFalse(commentRepository.existsById(comment.getId()));
    }
}
