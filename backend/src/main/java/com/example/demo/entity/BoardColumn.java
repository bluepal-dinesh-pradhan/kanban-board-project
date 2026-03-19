package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "board_columns",
    indexes = {
        @Index(name = "idx_board_columns_board_id", columnList = "board_id"),
        @Index(name = "idx_board_columns_board_archived", columnList = "board_id, archived"),
        @Index(name = "idx_board_columns_board_position", columnList = "board_id, position")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BoardColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int position;

    @Builder.Default
    private boolean archived = false;

    @OneToMany(mappedBy = "column", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<Card> cards = new ArrayList<>();
}
