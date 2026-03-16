package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardMembersDto {
    private List<BoardMemberDto> members;
    private List<InvitationDto> pendingInvitations;
}