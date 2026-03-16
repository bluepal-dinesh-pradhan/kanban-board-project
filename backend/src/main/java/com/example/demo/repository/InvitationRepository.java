package com.example.demo.repository;

import com.example.demo.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByEmailAndStatus(String email, Invitation.InvitationStatus status);
    List<Invitation> findByBoardId(Long boardId);
    boolean existsByBoardIdAndEmail(Long boardId, String email);
    Optional<Invitation> findByBoardIdAndEmail(Long boardId, String email);
    Optional<Invitation> findByToken(String token);
}
