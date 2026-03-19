package com.example.demo.repository;

import com.example.demo.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByEmailAndStatus(String email, Invitation.InvitationStatus status);
    List<Invitation> findByBoardId(Long boardId);
    boolean existsByBoardIdAndEmail(Long boardId, String email);
    Optional<Invitation> findByBoardIdAndEmail(Long boardId, String email);
    Optional<Invitation> findByBoardIdAndEmailAndStatus(Long boardId, String email, Invitation.InvitationStatus status);
    Optional<Invitation> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM Invitation i WHERE i.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
