package com.example.demo.repository;

import com.example.demo.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByColumnIdAndArchivedFalseOrderByPositionAsc(Long columnId);
    int countByColumnIdAndArchivedFalse(Long columnId);

    @Modifying
    @Query("UPDATE Card c SET c.position = c.position + 1 WHERE c.column.id = :colId AND c.position >= :pos AND c.archived = false")
    void incrementPositionsFrom(@Param("colId") Long columnId, @Param("pos") int position);

    @Modifying
    @Query("UPDATE Card c SET c.position = c.position - 1 WHERE c.column.id = :colId AND c.position > :pos AND c.archived = false")
    void decrementPositionsAfter(@Param("colId") Long columnId, @Param("pos") int position);

    @Modifying
    @Transactional
    @Query("DELETE FROM Card c WHERE c.column.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
    
    List<Card> findByColumnBoardIdAndArchivedTrue(Long boardId);
    
    List<Card> findByColumnBoardIdAndArchivedFalse(Long boardId);
}
