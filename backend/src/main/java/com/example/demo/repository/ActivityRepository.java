package com.example.demo.repository;

import com.example.demo.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findTop50ByBoardIdOrderByCreatedAtDesc(Long boardId);
    List<Activity> findByBoardIdOrderByCreatedAtDesc(Long boardId);
    Page<Activity> findByBoardIdOrderByCreatedAtDesc(Long boardId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Activity a WHERE a.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
