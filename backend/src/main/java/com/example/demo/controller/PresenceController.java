package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Slf4j
public class PresenceController {

    private final SimpMessagingTemplate messagingTemplate;

    // boardId -> Map<userId, {userInfo + lastSeen}>
    private static final Map<Long, Map<Long, Map<String, Object>>> boardPresence = new ConcurrentHashMap<>();

    // How many seconds before a user is considered "stale"
    private static final long STALE_SECONDS = 45;

    @PostMapping("/{boardId}/presence/join")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> joinBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserPrincipal user) {

        boardPresence.computeIfAbsent(boardId, k -> new ConcurrentHashMap<>());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getFullName() != null ? user.getFullName() : user.getEmail());
        userInfo.put("email", user.getEmail());
        userInfo.put("lastSeen", Instant.now().getEpochSecond());

        boardPresence.get(boardId).put(user.getId(), userInfo);

        // Clean stale users
        cleanStaleUsers(boardId);

        List<Map<String, Object>> onlineUsers = getCleanUserList(boardId);
        log.info("Presence: User {} joined board {} ({} users online)",
                user.getEmail(), boardId, onlineUsers.size());

        // Broadcast
        messagingTemplate.convertAndSend("/topic/board/" + boardId + "/presence", onlineUsers);

        return ResponseEntity.ok(ApiResponse.ok(onlineUsers));
    }

    // Heartbeat — frontend calls this every 30 seconds
    @PostMapping("/{boardId}/presence/heartbeat")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> heartbeat(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserPrincipal user) {

        Map<Long, Map<String, Object>> users = boardPresence.get(boardId);
        if (users != null) {
            Map<String, Object> existing = users.get(user.getId());
            if (existing != null) {
                existing.put("lastSeen", Instant.now().getEpochSecond());
            } else {
                // Re-join if somehow lost
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("name", user.getFullName() != null ? user.getFullName() : user.getEmail());
                userInfo.put("email", user.getEmail());
                userInfo.put("lastSeen", Instant.now().getEpochSecond());
                users.put(user.getId(), userInfo);
            }
        }

        // Clean stale users on every heartbeat
        cleanStaleUsers(boardId);

        List<Map<String, Object>> onlineUsers = getCleanUserList(boardId);

        // Broadcast updated list
        messagingTemplate.convertAndSend("/topic/board/" + boardId + "/presence", onlineUsers);

        return ResponseEntity.ok(ApiResponse.ok(onlineUsers));
    }

    @PostMapping("/{boardId}/presence/leave")
    public ResponseEntity<ApiResponse<String>> leaveBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserPrincipal user) {

        Map<Long, Map<String, Object>> users = boardPresence.get(boardId);
        if (users != null) {
            users.remove(user.getId());
            log.info("Presence: User {} left board {}", user.getEmail(), boardId);

            List<Map<String, Object>> onlineUsers = getCleanUserList(boardId);
            messagingTemplate.convertAndSend("/topic/board/" + boardId + "/presence", onlineUsers);

            if (users.isEmpty()) {
                boardPresence.remove(boardId);
            }
        }

        return ResponseEntity.ok(ApiResponse.ok("Left board"));
    }

    @GetMapping("/{boardId}/presence")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPresence(
            @PathVariable Long boardId) {
        cleanStaleUsers(boardId);
        List<Map<String, Object>> onlineUsers = getCleanUserList(boardId);
        return ResponseEntity.ok(ApiResponse.ok(onlineUsers));
    }

    // Remove users who haven't sent a heartbeat recently
    private void cleanStaleUsers(Long boardId) {
        Map<Long, Map<String, Object>> users = boardPresence.get(boardId);
        if (users == null) return;

        long now = Instant.now().getEpochSecond();
        List<Long> staleIds = users.entrySet().stream()
                .filter(e -> {
                    Object lastSeen = e.getValue().get("lastSeen");
                    if (lastSeen == null) return true;
                    return (now - (long) lastSeen) > STALE_SECONDS;
                })
                .map(Map.Entry::getKey)
                .toList();

        staleIds.forEach(id -> {
            users.remove(id);
            log.debug("Presence: Cleaned stale user {} from board {}", id, boardId);
        });

        if (users.isEmpty()) {
            boardPresence.remove(boardId);
        }
    }

    // Return user list without the internal "lastSeen" field
    private List<Map<String, Object>> getCleanUserList(Long boardId) {
        Map<Long, Map<String, Object>> users = boardPresence.getOrDefault(boardId, Collections.emptyMap());
        return users.values().stream()
                .map(u -> {
                    Map<String, Object> clean = new HashMap<>(u);
                    return clean;
                })
                .toList();
    }
}