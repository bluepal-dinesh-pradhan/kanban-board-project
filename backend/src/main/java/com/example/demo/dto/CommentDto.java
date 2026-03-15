package com.example.demo.dto;

import com.example.demo.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private UserDto author;
    private LocalDateTime createdAt;

    public static CommentDto from(Comment c) {
        return new CommentDto(c.getId(), c.getContent(), UserDto.from(c.getUser()), c.getCreatedAt());
    }
}
