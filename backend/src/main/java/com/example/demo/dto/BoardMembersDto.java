package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Board members and pending invitations.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardMembersDto {
    @Schema(description = "Current board members.")
    private List<BoardMemberDto> members;
    @Schema(description = "Pending invitations for the board.")
    private List<InvitationDto> pendingInvitations;
}
