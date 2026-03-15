package com.example.demo.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.BoardColumn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ColumnDto {
    private Long id;
    private String title;
    private int position;
    private List<CardDto> cards;

    public static ColumnDto from(BoardColumn c) {
        List<CardDto> cards = c.getCards().stream()
                .filter(card -> !card.isArchived())
                .map(CardDto::from)
                .collect(Collectors.toList());
        return new ColumnDto(c.getId(), c.getTitle(), c.getPosition(), cards);
    }
}
