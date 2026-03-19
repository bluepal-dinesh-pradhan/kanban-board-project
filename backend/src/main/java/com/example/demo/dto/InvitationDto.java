package com.example.demo.dto;

import com.example.demo.entity.Invitation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Schema(description = "Invitation summary.")
@Data @AllArgsConstructor @NoArgsConstructor
public class InvitationDto {
    @Schema(description = "Invitation id.")
    private Long id;
    @Schema(description = "Invited email address.")
    private String email;
    @Schema(description = "Role offered to the invitee.")
    private String role;
    @Schema(description = "Invitation status.")
    private String status;
    @Schema(description = "User who sent the invitation.")
    private UserDto invitedBy;
    @Schema(description = "Invitation creation timestamp.")
    private LocalDateTime createdAt;

    public static InvitationDto from(Invitation invitation) {
        return new InvitationDto(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole().name(),
                invitation.getStatus().name(),
                UserDto.from(invitation.getInvitedBy()),
                invitation.getCreatedAt()
        );
    }
}
