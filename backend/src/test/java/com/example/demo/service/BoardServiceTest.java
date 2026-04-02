package com.example.demo.service;

import com.example.demo.dto.BoardDto;
import com.example.demo.dto.BoardRequest;
import com.example.demo.dto.BoardUpdateRequest;
import com.example.demo.dto.InviteRequest;
import com.example.demo.entity.Board;
import com.example.demo.entity.BoardMember;
import com.example.demo.entity.User;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.BoardMemberRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private EmailService emailService;

    private User owner;

    @BeforeEach
    void setUp() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
        boardMemberRepository.deleteAll();

        owner = User.builder()
                .email("owner@example.com")
                .password("pass")
                .fullName("Owner")
                .build();
        owner = userRepository.saveAndFlush(owner);
    }

    @Test
    void createBoard_shouldSuccess() {
        BoardRequest req = new BoardRequest();
        req.setTitle("Project Board");
        req.setBackground("#000000");

        BoardDto response = boardService.create(req, owner.getId());

        assertNotNull(response);
        assertEquals("Project Board", response.getTitle());
        assertTrue(boardRepository.existsById(response.getId()));
        
        BoardMember member = boardMemberRepository.findByBoardIdAndUserId(response.getId(), owner.getId()).orElse(null);
        assertNotNull(member);
        assertEquals(BoardMember.Role.OWNER, member.getRole());
    }

    @Test
    void getBoard_withValidId_shouldReturnBoard() {
        Board board = Board.builder()
                .title("Board")
                .owner(owner)
                .build();
        board = boardRepository.saveAndFlush(board);
        
        BoardMember member = BoardMember.builder()
                .board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);

        BoardDto response = boardService.getBoard(board.getId(), owner.getId());

        assertNotNull(response);
        assertEquals("Board", response.getTitle());
    }

    @Test
    void getBoard_withInvalidId_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> boardService.getBoard(999L, owner.getId()));
    }

    @Test
    void deleteBoard_asOwner_shouldSuccess() {
        Board board = Board.builder()
                .title("To Delete")
                .owner(owner)
                .build();
        board = boardRepository.saveAndFlush(board);
        
        BoardMember member = BoardMember.builder()
                .board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);

        boardService.deleteBoard(board.getId(), owner.getId());

        assertFalse(boardRepository.existsById(board.getId()));
    }

    @Test
    void deleteBoard_asNonOwner_shouldThrowException() {
        User other = User.builder().email("other@example.com").password("pass").fullName("Other").build();
        other = userRepository.saveAndFlush(other);
        
        Board board = Board.builder()
                .title("Forbidden")
                .owner(owner)
                .build();
        board = boardRepository.saveAndFlush(board);
        
        BoardMember member = BoardMember.builder()
                .board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);
        
        BoardMember viewer = BoardMember.builder()
                .board(board).user(other).role(BoardMember.Role.VIEWER).build();
        boardMemberRepository.saveAndFlush(viewer);

        Long boardId = board.getId();
        Long otherId = other.getId();
        assertThrows(BadRequestException.class, () -> boardService.deleteBoard(boardId, otherId));
    }

    @Test
    void updateBoard_shouldSuccess() {
        Board board = Board.builder()
                .title("Old Title")
                .owner(owner)
                .build();
        board = boardRepository.saveAndFlush(board);
        
        BoardMember member = BoardMember.builder()
                .board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);

        BoardUpdateRequest req = new BoardUpdateRequest();
        req.setTitle("New Title");

        BoardDto response = boardService.updateBoard(board.getId(), req, owner.getId());

        assertEquals("New Title", response.getTitle());
        Board updated = boardRepository.findById(board.getId()).orElseThrow();
        assertEquals("New Title", updated.getTitle());
    }

    @Test
    void getUserBoards_shouldReturnList() {
        Board board = Board.builder().title("B").owner(owner).build();
        board = boardRepository.saveAndFlush(board);
        BoardMember member = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);

        var result = boardService.getUserBoards(owner.getId());
        assertEquals(1, result.size());
    }

    @Test
    void getBoardColumns_shouldReturnColumns() {
        Board board = Board.builder().title("B").owner(owner).build();
        board = boardRepository.saveAndFlush(board);
        BoardMember member = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);

        var result = boardService.getBoardColumns(board.getId(), owner.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    void toggleStar_shouldChangeStatus() {
        Board board = Board.builder().title("B").owner(owner).build();
        board = boardRepository.saveAndFlush(board);
        BoardMember member = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);

        boolean starred = boardService.toggleStar(board.getId(), owner.getId());
        assertTrue(starred);
        
        boolean unstarred = boardService.toggleStar(board.getId(), owner.getId());
        assertFalse(unstarred);
    }

    @Test
    void createFromTemplate_shouldCreateBoard() {
        BoardDto response = boardService.createFromTemplate("Kanban", "Agile", "Blue", owner.getId());
        assertNotNull(response);
        assertEquals("Kanban", response.getTitle());
    }

    @Test
    void boardAccess_asOwner_shouldPass() {
        Board b = Board.builder().title("B").owner(owner).build();
        final Board board = boardRepository.saveAndFlush(b);
        BoardMember member = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);

        assertDoesNotThrow(() -> boardService.checkPermission(board.getId(), owner.getId(), BoardMember.Role.OWNER));
    }

    @Test
    void inviteMember_shouldLink() {
        Board board = Board.builder().title("B").owner(owner).build();
        board = boardRepository.saveAndFlush(board);
        BoardMember member = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(member);

        InviteRequest req = new InviteRequest();
        req.setEmail("new@user.com");
        req.setRole(BoardMember.Role.EDITOR);
        assertNotNull(boardService.inviteMember(board.getId(), req, owner.getId()));
    }

    @Test
    @Transactional
    void getMembers_shouldReturnList() {
        Board board = Board.builder().title("B").owner(owner).members(new java.util.ArrayList<>()).build();
        board = boardRepository.saveAndFlush(board);
        BoardMember member = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        member = boardMemberRepository.saveAndFlush(member);
        
        // Handle laziness in test context
        board.getMembers().add(member);
        boardRepository.saveAndFlush(board);

        var members = boardService.getBoardMembers(board.getId(), owner.getId());
        assertEquals(1, members.getMembers().size());
    }

    @Test
    void removeMember_shouldSuccess() {
        User other = User.builder().email("other@x.com").password("p").fullName("O").build();
        other = userRepository.saveAndFlush(other);
        Board board = Board.builder().title("B").owner(owner).build();
        board = boardRepository.saveAndFlush(board);
        BoardMember m1 = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.saveAndFlush(m1);
        BoardMember m2 = BoardMember.builder().board(board).user(other).role(BoardMember.Role.VIEWER).build();
        boardMemberRepository.saveAndFlush(m2);

        boardService.removeMember(board.getId(), m2.getId(), owner.getId());
        assertFalse(boardMemberRepository.existsByBoardIdAndUserId(board.getId(), other.getId()));
    }

}
