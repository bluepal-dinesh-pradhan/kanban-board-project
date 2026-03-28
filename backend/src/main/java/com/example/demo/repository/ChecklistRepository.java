package com.example.demo.repository;

import com.example.demo.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    List<Checklist> findByCardIdOrderByPositionAsc(Long cardId);

    int countByCardId(Long cardId);

    int countByCardIdAndCompletedTrue(Long cardId);

    void deleteAllByCardId(Long cardId);
}