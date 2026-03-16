package com.example.demo.repository;

import com.example.demo.entity.CardLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface CardLabelRepository extends JpaRepository<CardLabel, Long> {
    @Modifying
    @Transactional
    void deleteAllByCardId(Long cardId);
}