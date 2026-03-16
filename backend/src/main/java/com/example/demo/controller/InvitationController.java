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

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<InvitationDetailsDto>> getInvitation(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok("Invitation details", invitationService.getInvitationDetails(token)));
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<ApiResponse<InvitationAcceptResponse>> acceptInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.ok("Invitation accepted", invitationService.acceptInvitation(token, user.getId())));
    }
}
