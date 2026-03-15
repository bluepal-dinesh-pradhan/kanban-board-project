package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    @Transactional
    public BoardDto create(BoardRequest req, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Board board = Board.builder().title(req.getTitle()).owner(user).build();
        board = boardRepository.save(board);

        // Add owner as member
        BoardMember member = BoardMember.builder()
                .board(board).user(user).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(member);

        // Create default columns
        String[] defaults = {"To Do", "In Progress", "Done"};
        for (int i = 0; i < defaults.length; i++) {
            BoardColumn col = BoardColumn.builder()
                    .board(board).title(defaults[i]).position(i).build();
            boardColumnRepository.save(col);
        }

        activityService.log(board, user, "CREATED_BOARD", "BOARD", board.getId());
        return BoardDto.from(board, "OWNER");
    }

    public List<BoardDto> getUserBoards(Long userId) {
        return boardRepository.findAllByMemberUserId(userId).stream()
                .map(b -> {
                    BoardMember m = boardMemberRepository.findByBoardIdAndUserId(b.getId(), userId).orElseThrow();
                    return BoardDto.from(b, m.getRole().name());
                }).collect(Collectors.toList());
    }

    @Transactional
    public BoardMember inviteMember(Long boardId, InviteRequest req, Long inviterId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        checkPermission(boardId, inviterId, BoardMember.Role.OWNER);

        User invitee = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + req.getEmail()));

        if (boardMemberRepository.existsByBoardIdAndUserId(boardId, invitee.getId())) {
            throw new RuntimeException("User is already a member");
        }

        BoardMember member = BoardMember.builder()
                .board(board).user(invitee).role(req.getRole()).build();
        member = boardMemberRepository.save(member);

        User inviter = userRepository.findById(inviterId).orElseThrow();
        activityService.log(board, inviter, "INVITED_MEMBER", "USER", invitee.getId());
        return member;
    }

    public List<ColumnDto> getBoardColumns(Long boardId, Long userId) {
        checkAccess(boardId, userId);
        return boardColumnRepository.findByBoardIdAndArchivedFalseOrderByPositionAsc(boardId)
                .stream().map(ColumnDto::from).collect(Collectors.toList());
    }

    public void checkAccess(Long boardId, Long userId) {
        if (!boardMemberRepository.existsByBoardIdAndUserId(boardId, userId)) {
            throw new RuntimeException("Access denied to this board");
        }
    }

    public void checkPermission(Long boardId, Long userId, BoardMember.Role minRole) {
        BoardMember m = boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        if (minRole == BoardMember.Role.OWNER && m.getRole() != BoardMember.Role.OWNER) {
            throw new RuntimeException("Only board owner can perform this action");
        }
        if (minRole == BoardMember.Role.EDITOR && m.getRole() == BoardMember.Role.VIEWER) {
            throw new RuntimeException("Viewers cannot perform this action");
        }
    }
}
