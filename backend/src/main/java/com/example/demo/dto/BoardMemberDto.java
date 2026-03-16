package com.example.demo.dto;

import com.example.demo.entity.BoardMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class BoardMemberDto {
    private Long id;
    private UserDto user;
    private String role;

    public static BoardMemberDto from(BoardMember member) {
        return new BoardMemberDto(
                member.getId(),
                UserDto.from(member.getUser()),
                member.getRole().name()
        );
    }
}