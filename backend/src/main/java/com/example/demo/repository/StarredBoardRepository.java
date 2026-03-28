package com.example.demo.repository;

import com.example.demo.entity.StarredBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StarredBoardRepository extends JpaRepository<StarredBoard, Long> {

    Optional<StarredBoard> findByUserIdAndBoardId(Long userId, Long boardId);

    boolean existsByUserIdAndBoardId(Long userId, Long boardId);

    List<StarredBoard> findByUserIdOrderByStarredAtDesc(Long userId);

    void deleteByUserIdAndBoardId(Long userId, Long boardId);
}