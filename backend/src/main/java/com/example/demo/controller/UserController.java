package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.NotificationPreferencesRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for user profile and settings")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Fetching profile for user: {}", userPrincipal.getEmail());
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .emailNotifications(user.isEmailNotifications())
                .dueDateReminders(user.isDueDateReminders())
                .boardInvitationEmails(user.isBoardInvitationEmails())
                .build());
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, String> request) {
        String fullName = request.get("fullName");
        log.info("Updating profile name for user: {} to {}", userPrincipal.getEmail(), fullName);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(fullName);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PatchMapping("/notification-preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPreferencesRequest> updateNotificationPreferences(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody NotificationPreferencesRequest request) {
        log.info("Updating notification preferences for user: {}", userPrincipal.getEmail());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailNotifications(request.isEmailNotifications());
        user.setDueDateReminders(request.isDueDateReminders());
        user.setBoardInvitationEmails(request.isBoardInvitationEmails());

        userRepository.save(user);

        return ResponseEntity.ok(NotificationPreferencesRequest.builder()
                .emailNotifications(user.isEmailNotifications())
                .dueDateReminders(user.isDueDateReminders())
                .boardInvitationEmails(user.isBoardInvitationEmails())
                .build());
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change user password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userPrincipal.getEmail());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.error("Current password incorrect for user: {}", userPrincipal.getEmail());
            return ResponseEntity.badRequest().body(ApiResponse.error("Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }
}
