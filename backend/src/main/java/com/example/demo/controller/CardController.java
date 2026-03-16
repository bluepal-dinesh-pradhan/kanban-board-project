package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.CardService;
import com.example.demo.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final CommentService commentService;

    @PostMapping("/boards/{boardId}/cards")
    public ResponseEntity<ApiResponse<CardDto>> createCard(
            @PathVariable Long boardId,
            @Valid @RequestBody CardRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Card created", cardService.create(boardId, req, user.getId())));
    }

    @PutMapping("/cards/{id}")
    public ResponseEntity<ApiResponse<CardDto>> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Card updated", cardService.update(id, req, user.getId())));
    }

    @PatchMapping("/cards/{id}/move")
    public ResponseEntity<ApiResponse<CardDto>> moveCard(
            @PathVariable Long id,
            @Valid @RequestBody MoveCardRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Card moved", cardService.move(id, req, user.getId())));
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<ApiResponse<CardDto>> getCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.getCard(id, user.getId())));
    }

    @PostMapping("/cards/{id}/comments")
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Comment added", commentService.addComment(id, req, user.getId())));
    }

    @PatchMapping("/cards/{id}/archive")
    public ResponseEntity<ApiResponse<CardDto>> archiveCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Card archived", cardService.archive(id, user.getId())));
    }

    @GetMapping("/cards/{id}/comments")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        // Check access before getting comments
        cardService.getCard(id, user.getId()); // This validates access
        return ResponseEntity.ok(ApiResponse.ok(cardService.getComments(id)));
    }
}

