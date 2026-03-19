package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Columns", description = "Board column management endpoints.")
public class ColumnController {

    private final ColumnService columnService;

    @Operation(
            summary = "Create column",
            description = "Creates a new column in a board."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Column created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid board"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/boards/{boardId}/columns")
    public ResponseEntity<ApiResponse<ColumnDto>> createColumn(
            @PathVariable Long boardId,
            @Valid @RequestBody ColumnRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        log.info("Create column requested for board {} by user {}", boardId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Column created", columnService.create(boardId, req, user.getId())));
    }

    @Operation(
            summary = "Update column",
            description = "Updates column title."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Column updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid column"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/columns/{columnId}")
    public ResponseEntity<ApiResponse<ColumnDto>> updateColumn(
            @PathVariable Long columnId,
            @Valid @RequestBody ColumnUpdateRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        log.info("Update column {} requested by user {}", columnId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Column updated", columnService.update(columnId, req, user.getId())));
    }

    @Operation(
            summary = "Delete column",
            description = "Permanently deletes a column and all its cards."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Column deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid column id or access error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/columns/{columnId}")
    public ResponseEntity<ApiResponse<String>> deleteColumn(
            @PathVariable Long columnId,
            @AuthenticationPrincipal UserPrincipal user) {
        log.info("Delete column {} requested by user {}", columnId, user.getId());
        columnService.delete(columnId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Column deleted successfully"));
    }
}
