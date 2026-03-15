package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"board_id", "user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BoardMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        OWNER, EDITOR, VIEWER
    }
}

