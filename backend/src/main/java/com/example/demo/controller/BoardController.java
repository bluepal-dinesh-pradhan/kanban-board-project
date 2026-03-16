package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ActivityService;
import com.example.demo.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BoardDto>>> getBoards(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getUserBoards(user.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BoardDto>> createBoard(
            @Valid @RequestBody BoardRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Board created", boardService.create(req, user.getId())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDto>> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Board updated", boardService.updateBoard(id, req, user.getId())));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<InviteResponse>> inviteMember(
            @PathVariable Long id,
            @Valid @RequestBody InviteRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        InviteResponse response = boardService.inviteMember(id, req, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<BoardMembersDto>> getBoardMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getBoardMembers(id, user.getId())));
    }

    @GetMapping("/{id}/columns")
    public ResponseEntity<ApiResponse<List<ColumnDto>>> getColumns(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getBoardColumns(id, user.getId())));
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<ApiResponse<List<ActivityDto>>> getActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        boardService.checkAccess(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(activityService.getByBoard(id)));
    }

    @DeleteMapping("/{id}/invitations/{invitationId}")
    public ResponseEntity<ApiResponse<String>> cancelInvitation(
            @PathVariable Long id,
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserPrincipal user) {
        boardService.cancelInvitation(id, invitationId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Invitation cancelled"));
    }
}

