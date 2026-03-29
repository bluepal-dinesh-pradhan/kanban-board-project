package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AttachmentDto;
import com.example.demo.entity.Attachment;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attachments", description = "File attachment endpoints.")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "Upload attachment", description = "Uploads a file to a card.")
    @PostMapping("/cards/{cardId}/attachments")
    public ResponseEntity<ApiResponse<AttachmentDto>> upload(
            @PathVariable Long cardId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user) throws IOException {
        AttachmentDto dto = attachmentService.upload(cardId, file, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("File uploaded", dto));
    }

    @Operation(summary = "Get attachments", description = "Returns all attachments for a card.")
    @GetMapping("/cards/{cardId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentDto>>> getAttachments(
            @PathVariable Long cardId) {
        return ResponseEntity.ok(ApiResponse.ok(attachmentService.getAttachments(cardId)));
    }

    @Operation(summary = "Download attachment", description = "Downloads an attachment file.")
    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        Attachment attachment = attachmentService.getAttachment(id);
        Path filePath = attachmentService.getFilePath(attachment);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @Operation(summary = "Delete attachment", description = "Deletes an attachment.")
    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        attachmentService.delete(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Attachment deleted"));
    }
}