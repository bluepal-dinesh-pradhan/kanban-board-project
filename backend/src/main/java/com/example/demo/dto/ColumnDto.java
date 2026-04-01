package com.example.demo.dto;

import java.util.List;


import com.example.demo.entity.BoardColumn;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Column details including cards.")
@Data @AllArgsConstructor @NoArgsConstructor
public class ColumnDto {
    @Schema(description = "Column id.")
    private Long id;
    @Schema(description = "Column title.")
    private String title;
    @Schema(description = "Column position in the board.")
    private int position;
    @Schema(description = "Cards in the column.")
    private List<CardDto> cards;

    public static ColumnDto from(BoardColumn c) {
        List<CardDto> cards = c.getCards().stream()
                .filter(card -> !card.isArchived())
                .map(CardDto::from)
                .toList();
        return new ColumnDto(c.getId(), c.getTitle(), c.getPosition(), cards);
    }
}
