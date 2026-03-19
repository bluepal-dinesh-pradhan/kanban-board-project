package com.example.demo.repository;

import com.example.demo.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findByBoardIdAndArchivedFalseOrderByPositionAsc(Long boardId);
    Page<BoardColumn> findByBoardIdAndArchivedFalse(Long boardId, Pageable pageable);
    int countByBoardIdAndArchivedFalse(Long boardId);

    @Modifying
    @Query("UPDATE BoardColumn c SET c.position = c.position - 1 WHERE c.board.id = :boardId AND c.position > :pos AND c.archived = false")
    void decrementPositionsAfter(@Param("boardId") Long boardId, @Param("pos") int position);

    @Modifying
    @Transactional
    @Query("DELETE FROM BoardColumn c WHERE c.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
