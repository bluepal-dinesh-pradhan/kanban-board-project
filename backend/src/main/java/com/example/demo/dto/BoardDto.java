package com.example.demo.dto;

import com.example.demo.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class BoardDto {
    private Long id;
    private String title;
    private UserDto owner;
    private String role;
    private LocalDateTime createdAt;

    public static BoardDto from(Board b, String role) {
        return new BoardDto(b.getId(), b.getTitle(), UserDto.from(b.getOwner()), role, b.getCreatedAt());
    }
}
