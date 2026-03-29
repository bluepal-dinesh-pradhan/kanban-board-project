package com.example.demo.repository;

import com.example.demo.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByCardIdOrderByUploadedAtDesc(Long cardId);

    int countByCardId(Long cardId);

    void deleteAllByCardId(Long cardId);
}