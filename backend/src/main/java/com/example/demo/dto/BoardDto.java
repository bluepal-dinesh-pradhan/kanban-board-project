package com.example.demo.dto;

import com.example.demo.entity.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Schema(description = "Board details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class BoardDto {
    @Schema(description = "Board id.")
    private Long id;
    @Schema(description = "Board title.")
    private String title;
    @Schema(description = "Board owner.")
    private UserDto owner;
    @Schema(description = "Role of the current user in the board.")
    private String role;
    @Schema(description = "Board background (color or image identifier).")
    private String background;
    @Schema(description = "Number of members on the board.")
    private int memberCount;
    @Schema(description = "Board creation timestamp.")
    private LocalDateTime createdAt;

    public static BoardDto from(Board b, String role) {
        return new BoardDto(
                b.getId(), 
                b.getTitle(), 
                UserDto.from(b.getOwner()), 
                role, 
                b.getBackground(),
                b.getMembers().size(),
                b.getCreatedAt()
        );
    }
}
