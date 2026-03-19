package com.example.demo.repository;

import com.example.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByCardIdOrderByCreatedAtDesc(Long cardId);
    Page<Comment> findByCardIdOrderByCreatedAtDesc(Long cardId, Pageable pageable);
    void deleteByCardId(Long cardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.card.id IN (SELECT ca.id FROM Card ca WHERE ca.column.board.id = :boardId)")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
