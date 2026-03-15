package com.example.demo.repository;

import com.example.demo.entity.CardLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardLabelRepository extends JpaRepository<CardLabel, Long> {
    void deleteAllByCardId(Long cardId);
}