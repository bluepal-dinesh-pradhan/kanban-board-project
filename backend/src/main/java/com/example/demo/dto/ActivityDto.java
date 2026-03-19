package com.example.demo.dto;

import com.example.demo.entity.Activity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Schema(description = "Activity log entry.")
@Data @AllArgsConstructor @NoArgsConstructor
public class ActivityDto {
    @Schema(description = "Activity id.")
    private Long id;
    @Schema(description = "Action performed.")
    private String action;
    @Schema(description = "Entity type affected by the action.")
    private String entityType;
    @Schema(description = "Entity id affected by the action.")
    private Long entityId;
    @Schema(description = "User who performed the action.")
    private UserDto user;
    @Schema(description = "Activity creation timestamp.")
    private LocalDateTime createdAt;

    public static ActivityDto from(Activity a) {
        return new ActivityDto(a.getId(), a.getAction(), a.getEntityType(),
                a.getEntityId(), UserDto.from(a.getUser()), a.getCreatedAt());
    }
}
