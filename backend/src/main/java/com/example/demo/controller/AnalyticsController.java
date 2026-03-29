package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Board analytics endpoints.")
public class AnalyticsController {

    private final BoardService boardService;
    private final BoardColumnRepository boardColumnRepository;
    private final CardRepository cardRepository;
    private final ActivityRepository activityRepository;
    private final BoardMemberRepository boardMemberRepository;

    @Operation(summary = "Get board analytics", description = "Returns analytics data for a board.")
    @GetMapping("/{boardId}/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserPrincipal user) {

        boardService.checkAccess(boardId, user.getId());
        log.info("Fetching analytics for board {} by user {}", boardId, user.getId());

        Map<String, Object> analytics = new LinkedHashMap<>();

        // 1. Cards by column
        List<BoardColumn> columns = boardColumnRepository.findByBoardIdAndArchivedFalseOrderByPositionAsc(boardId);
        List<Map<String, Object>> cardsByColumn = new ArrayList<>();
        int totalCards = 0;
        for (BoardColumn col : columns) {
            int count = cardRepository.countByColumnIdAndArchivedFalse(col.getId());
            totalCards += count;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("column", col.getTitle());
            entry.put("count", count);
            cardsByColumn.add(entry);
        }
        analytics.put("cardsByColumn", cardsByColumn);
        analytics.put("totalCards", totalCards);

        // 2. Cards by priority
        List<Card> allCards = cardRepository.findByColumnBoardIdAndArchivedFalse(boardId);
        Map<String, Long> priorityCounts = allCards.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getPriority() != null ? c.getPriority().name() : "NONE",
                        Collectors.counting()
                ));
        List<Map<String, Object>> cardsByPriority = new ArrayList<>();
        for (String p : List.of("URGENT", "HIGH", "MEDIUM", "LOW", "NONE")) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("priority", p);
            entry.put("count", priorityCounts.getOrDefault(p, 0L));
            cardsByPriority.add(entry);
        }
        analytics.put("cardsByPriority", cardsByPriority);

        // 3. Overdue cards count
        long overdueCount = allCards.stream()
                .filter(c -> c.getDueDate() != null && c.getDueDate().isBefore(java.time.LocalDate.now()))
                .count();
        analytics.put("overdueCards", overdueCount);

        // 4. Completion rate (cards in "Done"/"Completed" columns vs total)
        long doneCount = 0;
        for (BoardColumn col : columns) {
            String title = col.getTitle().toLowerCase();
            if (title.equals("done") || title.equals("completed") || title.equals("closed") || title.equals("resolved")) {
                doneCount += cardRepository.countByColumnIdAndArchivedFalse(col.getId());
            }
        }
        double completionRate = totalCards > 0 ? Math.round((doneCount * 100.0) / totalCards * 10) / 10.0 : 0;
        analytics.put("completedCards", doneCount);
        analytics.put("completionRate", completionRate);

        // 5. Cards with no due date
        long noDueDateCount = allCards.stream().filter(c -> c.getDueDate() == null).count();
        analytics.put("noDueDateCards", noDueDateCount);

        // 6. Cards with assignee vs unassigned
        long assignedCount = allCards.stream().filter(c -> c.getAssignee() != null).count();
        analytics.put("assignedCards", assignedCount);
        analytics.put("unassignedCards", totalCards - assignedCount);

        // 7. Member count
        long memberCount = boardMemberRepository.countByBoardId(boardId);
        analytics.put("memberCount", memberCount);

        // 8. Recent activity count (last 7 days)
        java.time.LocalDateTime weekAgo = java.time.LocalDateTime.now().minusDays(7);
        long recentActivityCount = activityRepository.countByBoardIdAndCreatedAtAfter(boardId, weekAgo);
        analytics.put("recentActivityCount", recentActivityCount);

        log.info("Analytics fetched for board {}", boardId);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }
}