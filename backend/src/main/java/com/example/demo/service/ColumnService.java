package com.example.demo.service;

import com.example.demo.dto.ColumnDto;
import com.example.demo.dto.ColumnRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class ColumnService {

    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;

    @Transactional
    public ColumnDto create(Long boardId, ColumnRequest req, Long userId) {
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        Board board = boardRepository.findById(boardId).orElseThrow();
        int pos = columnRepository.countByBoardIdAndArchivedFalse(boardId);

        BoardColumn col = BoardColumn.builder()
                .board(board).title(req.getTitle()).position(pos).build();
        col = columnRepository.save(col);

        User user = userRepository.findById(userId).orElseThrow();
        activityService.log(board, user, "CREATED_COLUMN", "COLUMN", col.getId());
        return ColumnDto.from(col);
    }
}
