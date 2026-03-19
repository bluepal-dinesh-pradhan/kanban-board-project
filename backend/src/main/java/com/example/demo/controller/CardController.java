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
            description = "Permanently deletes a card and its related data (comments, labels, reminders). Requires board editor permissions."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Card deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid card id or access error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse<String>> deleteCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserPrincipal user) {
        log.info("Deleting card {} requested by user {}", cardId, user.getId());
        cardService.delete(cardId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Card deleted successfully"));
    }
}
