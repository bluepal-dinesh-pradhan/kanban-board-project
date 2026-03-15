package com.example.demo.repository;

import com.example.demo.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findByBoardIdAndArchivedFalseOrderByPositionAsc(Long boardId);
    int countByBoardIdAndArchivedFalse(Long boardId);
}
