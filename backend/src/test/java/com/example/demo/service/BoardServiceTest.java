package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
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
    private BoardMemberRepository boardMemberRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private StarredBoardRepository starredBoardRepository;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private EmailService emailService;

    private User owner;

    @BeforeEach
    void setUp() {
        boardMemberRepository.deleteAll();
        boardColumnRepository.deleteAll();
        invitationRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        owner = User.builder()
                .email("owner@example.com")
                .password("password")
                .fullName("Owner")
                .build();
        owner = userRepository.save(owner);
    }

    @Test
    void createBoard_shouldSuccess() {
        BoardRequest req = new BoardRequest();
        req.setTitle("Project Board");
        req.setBackground("#FFFFFF");

        BoardDto response = boardService.create(req, owner.getId());

        assertNotNull(response);
        assertEquals("Project Board", response.getTitle());
        assertTrue(boardRepository.existsById(response.getId()));
        assertEquals(3, boardColumnRepository.findByBoardIdAndArchivedFalseOrderByPositionAsc(response.getId()).size());
        verify(activityService).log(any(), any(), eq("CREATED_BOARD"), eq("BOARD"), anyLong());
    }

    @Test
    void getUserBoards_shouldReturnList() {
        Board board = Board.builder().title("B").owner(owner).build();
        board = boardRepository.save(board);
        BoardMember m = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(m);

        List<BoardDto> list = boardService.getUserBoards(owner.getId());
        assertFalse(list.isEmpty());
    }

    @Test
    void getUserBoards_paged_shouldReturnPage() {
        Board board = Board.builder().title("B").owner(owner).build();
        board = boardRepository.save(board);
        BoardMember m = BoardMember.builder().board(board).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(m);

        PageResponse<BoardDto> page = boardService.getUserBoards(owner.getId(), 0, 10);
        assertNotNull(page);
    }

    @Test
    void inviteMember_existingUser_shouldAddDirectly() {
        Board board = Board.builder().title("B").owner(owner).build();
        final Board savedBoard = boardRepository.save(board);
        BoardMember m = BoardMember.builder().board(savedBoard).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(m);

        User invitee = User.builder().email("invitee@test.com").password("p").fullName("Invitee").build();
        final User savedInvitee = userRepository.save(invitee);

        InviteRequest req = new InviteRequest();
        req.setEmail("invitee@test.com");
        req.setRole(BoardMember.Role.EDITOR);

        InviteResponse resp = boardService.inviteMember(savedBoard.getId(), req, owner.getId());

        assertEquals("ADDED", resp.getStatus());
        assertTrue(boardMemberRepository.existsByBoardIdAndUserId(savedBoard.getId(), savedInvitee.getId()));
    }

    @Test
    void inviteMember_newUser_shouldCreateInvitation() {
        Board board = Board.builder().title("B").owner(owner).build();
        final Board savedBoard = boardRepository.save(board);
        BoardMember m = BoardMember.builder().board(savedBoard).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(m);

        InviteRequest req = new InviteRequest();
        req.setEmail("new@test.com"); req.setRole(BoardMember.Role.VIEWER);

        InviteResponse resp = boardService.inviteMember(savedBoard.getId(), req, owner.getId());

        assertEquals("INVITED", resp.getStatus());
        assertTrue(invitationRepository.existsByBoardIdAndEmail(savedBoard.getId(), "new@test.com"));
    }

    @Test
    void inviteMember_alreadyMember_shouldThrow() {
        Board board = Board.builder().title("B").owner(owner).build();
        final Board savedBoard = boardRepository.save(board);
        BoardMember m = BoardMember.builder().board(savedBoard).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(m);

        final InviteRequest req = new InviteRequest();
        req.setEmail(owner.getEmail()); req.setRole(BoardMember.Role.EDITOR);
        final Long uid = owner.getId();

        assertThrows(BadRequestException.class, () -> boardService.inviteMember(savedBoard.getId(), req, uid));
    }

    @Test
    void removeMember_shouldSuccess() {
        Board board = Board.builder().title("B").owner(owner).build();
        final Board savedBoard = boardRepository.save(board);
        BoardMember m1 = BoardMember.builder().board(savedBoard).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(m1);

        User other = User.builder().email("o@t.com").password("p").fullName("O").build();
        final User savedOther = userRepository.save(other);
        BoardMember m2 = BoardMember.builder().board(savedBoard).user(savedOther).role(BoardMember.Role.EDITOR).build();
        final BoardMember savedMember = boardMemberRepository.save(m2);

        boardService.removeMember(savedBoard.getId(), savedMember.getId(), owner.getId());

        assertFalse(boardMemberRepository.existsById(savedMember.getId()));
    }

    @Test
    void toggleStar_shouldWork() {
        Board board = Board.builder().title("B").owner(owner).build();
        final Board savedBoard = boardRepository.save(board);
        BoardMember m = BoardMember.builder().board(savedBoard).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(m);

        assertTrue(boardService.toggleStar(savedBoard.getId(), owner.getId())); // Starred
        assertTrue(starredBoardRepository.existsByUserIdAndBoardId(owner.getId(), savedBoard.getId()));
        
        assertFalse(boardService.toggleStar(savedBoard.getId(), owner.getId())); // Unstarred
        assertFalse(starredBoardRepository.existsByUserIdAndBoardId(owner.getId(), savedBoard.getId()));
    }

    @Test
    void createFromTemplate_SCRUM_shouldWork() {
        BoardDto dto = boardService.createFromTemplate("Scrum", null, "SCRUM", owner.getId());
        assertEquals(5, boardColumnRepository.findByBoardIdAndArchivedFalseOrderByPositionAsc(dto.getId()).size());
    }

    @Test
    void cancelInvitation_shouldSuccess() {
        Board board = Board.builder().title("B").owner(owner).build();
        final Board savedBoard = boardRepository.save(board);
        BoardMember m = BoardMember.builder().board(savedBoard).user(owner).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(m);

        Invitation inv = Invitation.builder()
                .board(savedBoard)
                .invitedBy(owner)
                .email("e")
                .role(BoardMember.Role.EDITOR)
                .status(Invitation.InvitationStatus.PENDING)
                .build();
        final Invitation savedInv = invitationRepository.save(inv);

        boardService.cancelInvitation(savedBoard.getId(), savedInv.getId(), owner.getId());
        assertFalse(invitationRepository.existsById(savedInv.getId()));
    }

    @Test
    void checkPermission_shouldThrowWhenLowRole() {
        Board board = Board.builder().title("B").owner(owner).build();
        final Board savedBoard = boardRepository.save(board);
        
        User viewer = User.builder().email("v@t.com").password("p").fullName("V").build();
        final User savedViewer = userRepository.save(viewer);
        BoardMember m = BoardMember.builder().board(savedBoard).user(savedViewer).role(BoardMember.Role.VIEWER).build();
        boardMemberRepository.save(m);

        final Long bid = savedBoard.getId();
        final Long uid = savedViewer.getId();
        assertThrows(BadRequestException.class, () -> boardService.checkPermission(bid, uid, BoardMember.Role.OWNER));
        assertThrows(BadRequestException.class, () -> boardService.checkPermission(bid, uid, BoardMember.Role.EDITOR));
    }
}
