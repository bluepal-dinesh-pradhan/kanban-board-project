package com.example.demo.repository;

import com.example.demo.entity.CardReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardReminderRepository extends JpaRepository<CardReminder, Long> {
    
    @Query("SELECT cr FROM CardReminder cr WHERE cr.reminderDateTime <= :now AND cr.triggered = false")
    List<CardReminder> findDueReminders(@Param("now") LocalDateTime now);
    
    List<CardReminder> findByCardIdAndUserId(Long cardId, Long userId);
    
    void deleteByCardId(Long cardId);
}
