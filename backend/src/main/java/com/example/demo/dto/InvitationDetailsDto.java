package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Invitation details by token.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDetailsDto {
    @Schema(description = "Invited email address.")
    private String email;
    @Schema(description = "Board id.")
    private Long boardId;
    @Schema(description = "Board title.")
    private String boardTitle;
    @Schema(description = "Role offered to the invitee.")
    private String role;
    @Schema(description = "Invitation status.")
    private String status;
    @Schema(description = "Invitation expiration time.")
    private LocalDateTime expiresAt;
    @Schema(description = "Whether a user already exists for the invited email.")
    private boolean userExists;
}
