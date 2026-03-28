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
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Tag(name = "Boards", description = "Board management endpoints.")
public class BoardController {

    private final BoardService boardService;
    private final ActivityService activityService;

    @Operation(
            summary = "Get user boards",
            description = "Returns all boards the authenticated user has access to."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Boards retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getBoards(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size) {
        if (page == null || size == null) {
            return ResponseEntity.ok(ApiResponse.ok(boardService.getUserBoards(user.getId())));
        }
        return ResponseEntity.ok(ApiResponse.ok(boardService.getUserBoards(user.getId(), page, size)));
    }

    @Operation(
            summary = "Create board",
            description = "Creates a new board owned by the authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Board created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BoardDto>> createBoard(
            @Valid @RequestBody BoardRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Board created", boardService.create(req, user.getId())));
    }

    @Operation(
            summary = "Get a board by ID",
            description = "Returns basic details for a specific board."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Board fetched"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Board not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDto>> getBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Board fetched", boardService.getBoard(id, user.getId())));
    }

    @Operation(
            summary = "Update board",
            description = "Updates board title or background."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Board updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid board"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDto>> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Board updated", boardService.updateBoard(id, req, user.getId())));
    }

    @Operation(
            summary = "Invite member",
            description = "Invites a user to join a board or adds them if they already exist."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation processed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid board"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<InviteResponse>> inviteMember(
            @PathVariable Long id,
            @Valid @RequestBody InviteRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        InviteResponse response = boardService.inviteMember(id, req, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    @Operation(
            summary = "Get board members",
            description = "Returns current members and pending invitations for a board."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Board members retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid board or access error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<BoardMembersDto>> getBoardMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getBoardMembers(id, user.getId())));
    }

    @Operation(
            summary = "Get board columns",
            description = "Returns columns and cards for a board."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Columns retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid board or access error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}/columns")
    public ResponseEntity<ApiResponse<?>> getColumns(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size) {
        if (page == null || size == null) {
            return ResponseEntity.ok(ApiResponse.ok(boardService.getBoardColumns(id, user.getId())));
        }
        return ResponseEntity.ok(ApiResponse.ok(boardService.getBoardColumns(id, user.getId(), page, size)));
    }

    @Operation(
            summary = "Get board activity",
            description = "Returns activity log entries for a board."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Activity retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid board or access error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}/activity")
    public ResponseEntity<ApiResponse<?>> getActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size) {
        boardService.checkAccess(id, user.getId());
        if (page == null || size == null) {
            return ResponseEntity.ok(ApiResponse.ok(activityService.getByBoard(id)));
        }
        return ResponseEntity.ok(ApiResponse.ok(activityService.getByBoard(id, page, size)));
    }

    @Operation(
            summary = "Cancel invitation",
            description = "Cancels a pending invitation for a board."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation cancelled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid invitation or access error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}/invitations/{invitationId}")
    public ResponseEntity<ApiResponse<String>> cancelInvitation(
            @PathVariable Long id,
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserPrincipal user) {
        boardService.cancelInvitation(id, invitationId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Invitation cancelled"));
    }

    @Operation(
            summary = "Remove member",
            description = "Removes a member from a board."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid member or access error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponse<String>> removeMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal user) {
        boardService.removeMember(id, memberId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Member removed"));
    }
    
    @Operation(summary = "Toggle star", description = "Stars or unstars a board for the current user.")
    @PostMapping("/{boardId}/star")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleStar(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserPrincipal user) {
        boolean starred = boardService.toggleStar(boardId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(
                starred ? "Board starred" : "Board unstarred",
                Map.of("starred", starred)
        ));
    }

    @Operation(summary = "Get starred board IDs", description = "Returns list of board IDs starred by the current user.")
    @GetMapping("/starred")
    public ResponseEntity<ApiResponse<List<Long>>> getStarredBoards(
            @AuthenticationPrincipal UserPrincipal user) {
        List<Long> ids = boardService.getStarredBoardIds(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(ids));
    }
    
    @Operation(summary = "Create board from template", description = "Creates a new board with preset columns based on a template type.")
    @PostMapping("/from-template")
    public ResponseEntity<ApiResponse<BoardDto>> createFromTemplate(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal user) {
        String title = body.get("title");
        String background = body.get("background");
        String template = body.get("template");

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Title is required", null));
        }
        if (template == null || template.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Template type is required", null));
        }

        BoardDto dto = boardService.createFromTemplate(title.trim(), background, template.trim(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Board created from template", dto));
    }

    @Operation(
            summary = "Delete board",
            description = "Permanently deletes a board and all its related data. Only the board owner can perform this action."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Board deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Board not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        boardService.deleteBoard(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Board deleted successfully"));
    }
}

