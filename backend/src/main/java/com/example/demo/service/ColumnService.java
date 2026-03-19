package com.example.demo.service;

import com.example.demo.dto.ColumnDto;
import com.example.demo.dto.ColumnRequest;
import com.example.demo.dto.ColumnUpdateRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
@Slf4j
public class ColumnService {

    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;

    @Transactional
    public ColumnDto create(Long boardId, ColumnRequest req, Long userId) {
        log.info("Creating column '{}' on board {} for user {}", req.getTitle(), boardId, userId);
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        int pos = columnRepository.countByBoardIdAndArchivedFalse(boardId);
        log.debug("Board {} currently has {} columns", boardId, pos);

        BoardColumn col = BoardColumn.builder()
                .board(board).title(req.getTitle()).position(pos).build();
        col = columnRepository.save(col);

        User user = userRepository.findById(userId).orElseThrow();
        activityService.log(board, user, "CREATED_COLUMN", "COLUMN", col.getId());
        ColumnDto dto = ColumnDto.from(col);
        log.info("Column '{}' created successfully with id {}", col.getTitle(), col.getId());
        return dto;
    }

    @Transactional
    public ColumnDto update(Long columnId, ColumnUpdateRequest req, Long userId) {
        log.info("Updating column {} by user {}", columnId, userId);
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));
        Long boardId = column.getBoard().getId();

        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        column.setTitle(req.getTitle());

        column = columnRepository.save(column);
        ColumnDto dto = ColumnDto.from(column);
        log.info("Column {} updated successfully", columnId);
        return dto;
    }

    @Transactional
    public void delete(Long columnId, Long userId) {
        log.info("Deleting column {} by user {}", columnId, userId);
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));
        Long boardId = column.getBoard().getId();
        int position = column.getPosition();
        boolean archived = column.isArchived();
        String title = column.getTitle();

        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        activityService.log(board, user, "Deleted column: " + title, "COLUMN", columnId);
        columnRepository.delete(column);

        if (!archived) {
            columnRepository.decrementPositionsAfter(boardId, position);
        }

        log.info("Column {} deleted successfully", columnId);
    }
}
