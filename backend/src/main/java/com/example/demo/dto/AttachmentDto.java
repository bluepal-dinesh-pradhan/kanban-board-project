package com.example.demo.dto;

import com.example.demo.entity.Attachment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Attachment details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class AttachmentDto {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadedByName;
    private Long uploadedById;
    private LocalDateTime uploadedAt;
    private String downloadUrl;

    public static AttachmentDto from(Attachment a) {
        AttachmentDto dto = new AttachmentDto();
        dto.setId(a.getId());
        dto.setFileName(a.getFileName());
        dto.setFileType(a.getFileType());
        dto.setFileSize(a.getFileSize());
        dto.setUploadedAt(a.getUploadedAt());
        dto.setDownloadUrl("/api/attachments/" + a.getId() + "/download");
        if (a.getUploadedBy() != null) {
            dto.setUploadedByName(a.getUploadedBy().getFullName());
            dto.setUploadedById(a.getUploadedBy().getId());
        }
        return dto;
    }
}