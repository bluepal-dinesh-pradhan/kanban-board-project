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

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<String>> inviteMember(
            @PathVariable Long id,
            @Valid @RequestBody InviteRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        boardService.inviteMember(id, req, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Member invited", "Member invited successfully"));
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
}

