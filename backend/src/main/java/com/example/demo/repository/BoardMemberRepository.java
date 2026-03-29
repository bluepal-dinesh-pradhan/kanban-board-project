package com.example.demo.repository;

import com.example.demo.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {
    Optional<BoardMember> findByBoardIdAndUserId(Long boardId, Long userId);
    boolean existsByBoardIdAndUserId(Long boardId, Long userId);
    Optional<BoardMember> findByIdAndBoardId(Long id, Long boardId);
    List<BoardMember> findByBoardId(Long boardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BoardMember bm WHERE bm.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
    
    long countByBoardId(Long boardId);
}
