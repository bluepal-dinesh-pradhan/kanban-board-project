package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/{boardId}/columns")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;

    @PostMapping
    public ResponseEntity<ApiResponse<ColumnDto>> createColumn(
            @PathVariable Long boardId,
            @Valid @RequestBody ColumnRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Column created", columnService.create(boardId, req, user.getId())));
    }
}
