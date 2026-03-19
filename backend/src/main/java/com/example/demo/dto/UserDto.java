package com.example.demo.dto;

import com.example.demo.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "User details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class UserDto {
    @Schema(description = "User id.")
    private Long id;
    @Schema(description = "User email address.")
    private String email;
    @Schema(description = "User full name.")
    private String fullName;

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getFullName());
    }
}
