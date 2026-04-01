package com.example.demo.service;

import com.example.demo.dto.ColumnDto;
import com.example.demo.dto.ColumnRequest;
import com.example.demo.dto.ColumnUpdateRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BadRequestException;


@Service @RequiredArgsConstructor
@Slf4j
public class ColumnService {

    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;
    private final WebSocketNotificationService webSocketNotificationService; // NEW

    private static final String COLUMN_NOT_FOUND = "Column not found";
    private static final String BOARD_NOT_FOUND = "Board not found";
    private static final String USER_NOT_FOUND = "User not found";

    @Transactional
    public ColumnDto create(Long boardId, ColumnRequest req, Long userId) {
        log.info("Creating column '{}' on board {} for user {}", req.getTitle(), boardId, userId);
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        int pos = columnRepository.countByBoardIdAndArchivedFalse(boardId);
        log.debug("Board {} currently has {} columns", boardId, pos);

        BoardColumn col = BoardColumn.builder()
                .board(board).title(req.getTitle()).position(pos).build();
        col = columnRepository.save(col);

        User user = userRepository.findById(userId).orElseThrow();
        activityService.log(board, user, "CREATED_COLUMN", "COLUMN", col.getId());
        ColumnDto dto = ColumnDto.from(col);
        log.info("Column '{}' created successfully with id {}", col.getTitle(), col.getId());

        // NEW: Broadcast real-time event
        broadcastSafely(boardId, "column.created", userId, user.getFullName(), dto);

        return dto;
    }

    @Transactional
    public ColumnDto update(Long columnId, ColumnUpdateRequest req, Long userId) {
        log.info("Updating column {} by user {}", columnId, userId);
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException(COLUMN_NOT_FOUND));
        Long boardId = column.getBoard().getId();

        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        column.setTitle(req.getTitle());

        column = columnRepository.save(column);

        // NEW: Broadcast real-time event
        User user = userRepository.findById(userId).orElseThrow();
        ColumnDto dto = ColumnDto.from(column);
        broadcastSafely(boardId, "column.updated", userId, user.getFullName(), dto);

        log.info("Column {} updated successfully", columnId);
        return dto;
    }

    @Transactional
    public void delete(Long columnId, Long userId) {
        log.info("Deleting column {} by user {}", columnId, userId);
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException(COLUMN_NOT_FOUND));
        Long boardId = column.getBoard().getId();
        int position = column.getPosition();
        boolean archived = column.isArchived();
        String title = column.getTitle();

        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        activityService.log(board, user, "Deleted column: " + title, "COLUMN", columnId);
        columnRepository.delete(column);

        if (!archived) {
            columnRepository.decrementPositionsAfter(boardId, position);
        }

        log.info("Column {} deleted successfully", columnId);

        // NEW: Broadcast real-time event
        Map<String, Object> deletePayload = Map.of(
            "columnId", columnId,
            "title", title
        );
        broadcastSafely(boardId, "column.deleted", userId, user.getFullName(), deletePayload);
    }

    @Transactional
    public void moveColumn(Long columnId, int newPosition, Long userId) {
        log.info("Moving column {} to position {} by user {}", columnId, newPosition, userId);
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException(COLUMN_NOT_FOUND));
        Long boardId = column.getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        List<BoardColumn> columns = columnRepository.findByBoardIdAndArchivedFalseOrderByPositionAsc(boardId);
        
        int actualOldIndex = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getId().equals(columnId)) {
                actualOldIndex = i;
                break;
            }
        }
        
        if (actualOldIndex == -1) return;
        if (actualOldIndex == newPosition) return;

        BoardColumn movedCol = columns.remove(actualOldIndex);
        columns.add(Math.min(newPosition, columns.size()), movedCol);

        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setPosition(i);
        }
        columnRepository.saveAll(columns);
        log.info("Column {} moved to position {} successfully", columnId, newPosition);

        // NEW: Broadcast real-time event
        User user = userRepository.findById(userId).orElseThrow();
        Map<String, Object> movePayload = Map.of(
            "columnId", columnId,
            "newPosition", newPosition
        );
        broadcastSafely(boardId, "column.moved", userId, user.getFullName(), movePayload);
    }

    // NEW: Safe WebSocket broadcast helper
    private void broadcastSafely(Long boardId, String eventType, Long userId, String userName, Object payload) {
        try {
            webSocketNotificationService.broadcastBoardEvent(boardId, eventType, userId, userName, payload);
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for {} on board {}: {}", eventType, boardId, e.getMessage());
        }
    }
}