package com.example.demo.repository;

import com.example.demo.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b JOIN b.members m WHERE m.user.id = :userId AND b.archived = false")
    List<Board> findAllByMemberUserId(@Param("userId") Long userId);
}
