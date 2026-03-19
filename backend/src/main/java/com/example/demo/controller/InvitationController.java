package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.InvitationAcceptResponse;
import com.example.demo.dto.InvitationDetailsDto;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Board invitation endpoints.")
public class InvitationController {

    private final InvitationService invitationService;

    @Operation(
            summary = "Get invitation details",
            description = "Retrieves invitation details using the invitation token.",
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired invitation token")
    })
    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<InvitationDetailsDto>> getInvitation(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok("Invitation details", invitationService.getInvitationDetails(token)));
    }

    @Operation(
            summary = "Accept invitation",
            description = "Accepts an invitation for the authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired invitation token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{token}/accept")
    public ResponseEntity<ApiResponse<InvitationAcceptResponse>> acceptInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Invitation accepted", invitationService.acceptInvitation(token, user.getId())));
    }
}
