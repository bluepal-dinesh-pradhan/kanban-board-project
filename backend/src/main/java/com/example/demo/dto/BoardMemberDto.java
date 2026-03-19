package com.example.demo.dto;

import com.example.demo.entity.BoardMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Board member details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class BoardMemberDto {
    @Schema(description = "Board member id.")
    private Long id;
    @Schema(description = "User details.")
    private UserDto user;
    @Schema(description = "Member role within the board.")
    private String role;

    public static BoardMemberDto from(BoardMember member) {
        return new BoardMemberDto(
                member.getId(),
                UserDto.from(member.getUser()),
                member.getRole().name()
        );
    }
}
