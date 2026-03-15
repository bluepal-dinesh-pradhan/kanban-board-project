package com.example.demo.dto;

import com.example.demo.entity.Activity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class ActivityDto {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private UserDto user;
    private LocalDateTime createdAt;

    public static ActivityDto from(Activity a) {
        return new ActivityDto(a.getId(), a.getAction(), a.getEntityType(),
                a.getEntityId(), UserDto.from(a.getUser()), a.getCreatedAt());
    }
}