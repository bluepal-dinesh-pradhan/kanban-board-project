package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "starred_boards",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "board_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StarredBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false, updatable = false)
    private LocalDateTime starredAt;

    @PrePersist
    protected void onCreate() {
        starredAt = LocalDateTime.now();
    }
}