package com.example.demo.service;

import com.example.demo.dto.AttachmentDto;
import com.example.demo.entity.*;
import com.example.demo.repository.AttachmentRepository;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;
    private final WebSocketNotificationService webSocketNotificationService;

    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Transactional
    public AttachmentDto upload(Long cardId, MultipartFile file, Long userId) throws IOException {
        log.info("Uploading file '{}' to card {} by user {}", file.getOriginalFilename(), cardId, userId);

        Card card = cardRepository.findById(cardId).orElseThrow();
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique stored name
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + extension;

        // Save file to disk
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath);

        // Save to database
        User user = userRepository.findById(userId).orElseThrow();
        Attachment attachment = Attachment.builder()
                .card(card)
                .fileName(originalName)
                .storedName(storedName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(user)
                .build();
        attachment = attachmentRepository.save(attachment);

        Board board = card.getColumn().getBoard();
        activityService.log(board, user, "UPLOADED_FILE", "CARD", cardId);

        AttachmentDto dto = AttachmentDto.from(attachment);
        log.info("File '{}' uploaded to card {} as attachment {}", originalName, cardId, attachment.getId());

        broadcastSafely(boardId, "card.updated", userId, user.getFullName(),
                Map.of("cardId", cardId, "attachment", "uploaded"));

        return dto;
    }

    public List<AttachmentDto> getAttachments(Long cardId) {
        return attachmentRepository.findByCardIdOrderByUploadedAtDesc(cardId)
                .stream().map(AttachmentDto::from).collect(Collectors.toList());
    }

    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    public Path getFilePath(Attachment attachment) {
        return Paths.get(UPLOAD_DIR).resolve(attachment.getStoredName());
    }

    @Transactional
    public void delete(Long attachmentId, Long userId) {
        log.info("Deleting attachment {} by user {}", attachmentId, userId);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        Long boardId = attachment.getCard().getColumn().getBoard().getId();
        Long cardId = attachment.getCard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        // Delete file from disk
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(attachment.getStoredName());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file from disk: {}", e.getMessage());
        }

        User user = userRepository.findById(userId).orElseThrow();
        attachmentRepository.delete(attachment);
        log.info("Attachment {} deleted", attachmentId);

        broadcastSafely(boardId, "card.updated", userId, user.getFullName(),
                Map.of("cardId", cardId, "attachment", "deleted"));
    }

    private void broadcastSafely(Long boardId, String eventType, Long userId, String userName, Object payload) {
        try {
            webSocketNotificationService.broadcastBoardEvent(boardId, eventType, userId, userName, payload);
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed: {}", e.getMessage());
        }
    }
}