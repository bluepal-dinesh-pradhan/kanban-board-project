package com.example.demo.repository;

import com.example.demo.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {
    Optional<BoardMember> findByBoardIdAndUserId(Long boardId, Long userId);
    boolean existsByBoardIdAndUserId(Long boardId, Long userId);
    Optional<BoardMember> findByIdAndBoardId(Long id, Long boardId);
    List<BoardMember> findByBoardId(Long boardId);
}
