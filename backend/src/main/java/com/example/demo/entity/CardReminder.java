package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "card_reminders",
    indexes = {
        @Index(name = "idx_card_reminders_card_id", columnList = "card_id"),
        @Index(name = "idx_card_reminders_card_user", columnList = "card_id, user_id"),
        @Index(name = "idx_card_reminders_due_triggered", columnList = "reminder_date_time, triggered")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CardReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType reminderType;

    @Column(nullable = false)
    private LocalDateTime reminderDateTime;

    @Builder.Default
    @Column(nullable = false)
    private boolean triggered = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ReminderType {
        AT_DUE_TIME("At due time"),
        ONE_DAY_BEFORE("1 day before"),
        TWO_DAYS_BEFORE("2 days before"),
        ONE_WEEK_BEFORE("1 week before");

        private final String displayName;

        ReminderType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
