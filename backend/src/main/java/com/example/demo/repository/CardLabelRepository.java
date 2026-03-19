package com.example.demo.repository;

import com.example.demo.entity.CardLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CardLabelRepository extends JpaRepository<CardLabel, Long> {
    @Modifying
    @Transactional
    void deleteAllByCardId(Long cardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CardLabel cl WHERE cl.card.id IN (SELECT c.id FROM Card c WHERE c.column.board.id = :boardId)")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
