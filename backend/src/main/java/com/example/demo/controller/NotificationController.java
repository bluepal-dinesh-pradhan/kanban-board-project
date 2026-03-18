package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.NotificationDto;
import com.example.demo.service.NotificationService;
import com.example.demo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Unread count retrieved", count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(notificationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}