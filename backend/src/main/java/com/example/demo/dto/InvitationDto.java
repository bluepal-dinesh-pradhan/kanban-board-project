package com.example.demo.dto;

import com.example.demo.entity.Invitation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class InvitationDto {
    private Long id;
    private String email;
    private String role;
    private String status;
    private UserDto invitedBy;
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