package com.example.demo.dto;

import com.example.demo.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Schema(description = "Comment details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class CommentDto {
    @Schema(description = "Comment id.")
    private Long id;
    @Schema(description = "Comment content.")
    private String content;
    @Schema(description = "Author of the comment.")
    private UserDto author;
    @Schema(description = "Comment creation timestamp.")
    private LocalDateTime createdAt;

    public static CommentDto from(Comment c) {
        return new CommentDto(c.getId(), c.getContent(), UserDto.from(c.getUser()), c.getCreatedAt());
    }
}
