package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ChecklistDto;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ChecklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Checklists", description = "Checklist/subtask management endpoints.")
public class ChecklistController {

    private final ChecklistService checklistService;

    @Operation(summary = "Get checklist items", description = "Returns all checklist items for a card.")
    @GetMapping("/cards/{cardId}/checklists")
    public ResponseEntity<ApiResponse<List<ChecklistDto>>> getItems(
            @PathVariable Long cardId) {
        return ResponseEntity.ok(ApiResponse.ok(checklistService.getItems(cardId)));
    }

    @Operation(summary = "Add checklist item", description = "Adds a new checklist item to a card.")
    @PostMapping("/cards/{cardId}/checklists")
    public ResponseEntity<ApiResponse<ChecklistDto>> addItem(
            @PathVariable Long cardId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal user) {
        String title = body.get("title");
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Title is required", null));
        }
        ChecklistDto dto = checklistService.addItem(cardId, title.trim(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Checklist item added", dto));
    }

    @Operation(summary = "Toggle checklist item", description = "Toggles completed status of a checklist item.")
    @PatchMapping("/checklists/{itemId}/toggle")
    public ResponseEntity<ApiResponse<ChecklistDto>> toggleItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal user) {
        ChecklistDto dto = checklistService.toggleItem(itemId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Item toggled", dto));
    }

    @Operation(summary = "Update checklist item", description = "Updates the title of a checklist item.")
    @PatchMapping("/checklists/{itemId}")
    public ResponseEntity<ApiResponse<ChecklistDto>> updateItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal user) {
        String title = body.get("title");
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Title is required", null));
        }
        ChecklistDto dto = checklistService.updateItem(itemId, title.trim(), user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Item updated", dto));
    }

    @Operation(summary = "Delete checklist item", description = "Deletes a checklist item.")
    @DeleteMapping("/checklists/{itemId}")
    public ResponseEntity<ApiResponse<String>> deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal user) {
        checklistService.deleteItem(itemId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Checklist item deleted"));
    }
}