package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.CardService;
import com.example.demo.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cards", description = "Card management endpoints.")
public class CardController {

    private final CardService cardService;
    private final CommentService commentService;

    @Operation(summary = "Get card details", description = "Returns details for a single card.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardDto>> getCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.getCard(id, user.getId())));
    }

    @Operation(summary = "Create card", description = "Creates a new card on a board.")
    @PostMapping("/boards/{boardId}")
    public ResponseEntity<ApiResponse<CardDto>> createCard(
            @PathVariable Long boardId,
            @Valid @RequestBody CardRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Card created", cardService.create(boardId, req, user.getId())));
    }

    @Operation(summary = "Update card", description = "Updates card details.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CardDto>> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Card updated", cardService.update(id, req, user.getId())));
    }

    // NEW: Quick priority update endpoint
    @Operation(summary = "Update card priority", description = "Updates only the priority of a card.")
    @PatchMapping("/{cardId}/priority")
    public ResponseEntity<ApiResponse<CardDto>> updatePriority(
            @PathVariable Long cardId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal user) {
        String priority = body.get("priority");
        if (priority == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Priority is required", null));
        }
        CardDto dto = cardService.updatePriority(cardId, priority, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Priority updated", dto));
    }
    
 // ============================================================
    // ADD this endpoint to your existing CardController.java
    // Place it after the updatePriority() endpoint
    // ============================================================

    @Operation(summary = "Assign card", description = "Assigns a board member to a card, or unassigns if assigneeId is null.")
    @PatchMapping("/{cardId}/assign")
    public ResponseEntity<ApiResponse<CardDto>> assignCard(
            @PathVariable Long cardId,
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserPrincipal user) {
        Long assigneeId = body.get("assigneeId"); // null = unassign
        CardDto dto = cardService.assignCard(cardId, assigneeId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Card assigned", dto));
    }
    
    
 // ============================================================
    // ADD these 3 endpoints to your existing CardController.java
    // Place them after the assignCard endpoint
    // ============================================================

    @Operation(summary = "Duplicate card", description = "Creates a copy of a card with all labels.")
    @PostMapping("/{cardId}/duplicate")
    public ResponseEntity<ApiResponse<CardDto>> duplicateCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserPrincipal user) {
        CardDto dto = cardService.duplicate(cardId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Card duplicated", dto));
    }

    @Operation(summary = "Restore card", description = "Restores an archived card back to its column.")
    @PostMapping("/{cardId}/restore")
    public ResponseEntity<ApiResponse<CardDto>> restoreCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserPrincipal user) {
        CardDto dto = cardService.restore(cardId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Card restored", dto));
    }

    @Operation(summary = "Get archived cards", description = "Returns all archived cards for a board.")
    @GetMapping("/boards/{boardId}/archived")
    public ResponseEntity<ApiResponse<List<CardDto>>> getArchivedCards(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserPrincipal user) {
        List<CardDto> cards = cardService.getArchivedCards(boardId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(cards));
    }
    

    @Operation(summary = "Move card", description = "Moves a card to a different position or column.")
    @PostMapping("/{id}/move")
    public ResponseEntity<ApiResponse<CardDto>> moveCard(
            @PathVariable Long id,
            @Valid @RequestBody MoveCardRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Card moved", cardService.move(id, req, user.getId())));
    }

    @Operation(summary = "Archive card", description = "Moves a card into archived state.")
    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<CardDto>> archiveCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Card archived", cardService.archive(id, user.getId())));
    }

    @Operation(summary = "Get comments", description = "Returns comments for a card.")
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<?>> getComments(
            @PathVariable Long id,
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size) {
        if (page == null || size == null) {
            return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(id)));
        }
        return ResponseEntity.ok(ApiResponse.ok(cardService.getComments(id, page, size)));
    }

    @Operation(summary = "Add comment", description = "Adds a new comment to a card.")
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Comment added", commentService.addComment(id, req, user.getId())));
    }

    @Operation(
            summary = "Delete comment",
            description = "Deletes a comment. Only the comment author or the board owner can delete a comment."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/boards/{boardId}/cards/{id}/comments/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long id,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal user) {
        commentService.deleteComment(boardId, id, commentId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Comment deleted successfully"));
    }

    @Operation(
            summary = "Delete card",
            description = "Permanently deletes a card and its related data."
    )
    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse<String>> deleteCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserPrincipal user) {
        log.info("Deleting card {} requested by user {}", cardId, user.getId());
        cardService.delete(cardId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Card deleted successfully"));
    }
}