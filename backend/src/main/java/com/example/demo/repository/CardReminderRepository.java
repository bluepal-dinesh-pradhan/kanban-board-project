package com.example.demo.repository;

import com.example.demo.entity.CardReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardReminderRepository extends JpaRepository<CardReminder, Long> {
    
    @Query("SELECT cr FROM CardReminder cr WHERE cr.reminderDateTime <= :now AND cr.triggered = false")
    List<CardReminder> findDueReminders(@Param("now") LocalDateTime now);
    
    List<CardReminder> findByCardIdAndUserId(Long cardId, Long userId);
    
    void deleteByCardId(Long cardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CardReminder cr WHERE cr.card.id IN (SELECT c.id FROM Card c WHERE c.column.board.id = :boardId)")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
