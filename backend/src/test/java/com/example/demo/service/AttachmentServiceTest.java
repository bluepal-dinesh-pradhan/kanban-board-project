package com.example.demo.service;

import com.example.demo.dto.AttachmentDto;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AttachmentRepository;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardService boardService;
    @Mock
    private ActivityService activityService;
    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @InjectMocks
    private AttachmentService attachmentService;

    private final String UPLOAD_DIR = "uploads";

    @BeforeEach
    @AfterEach
    void cleanup() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (Files.exists(uploadPath)) {
            Files.walk(uploadPath)
                .sorted((p1, p2) -> p2.compareTo(p1))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                    }
                });
        }
    }

    @Test
    void upload_shouldSuccess() throws IOException {
        User user = new User();
        user.setId(1L);
        user.setFullName("User");
        Board board = new Board();
        board.setId(10L);
        BoardColumn column = new BoardColumn();
        column.setBoard(board);
        Card card = new Card();
        card.setId(2L);
        card.setColumn(column);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        when(cardRepository.findById(2L)).thenReturn(Optional.of(card));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(attachmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AttachmentDto result = attachmentService.upload(2L, file, 1L);

        assertNotNull(result);
        assertEquals("test.txt", result.getFileName());
        verify(boardService).checkPermission(10L, 1L, BoardMember.Role.EDITOR);
        verify(attachmentRepository).save(any());
        verify(activityService).log(any(), any(), eq("UPLOADED_FILE"), eq("CARD"), eq(2L));
    }

    @Test
    void upload_emptyFile_shouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        Card card = new Card();
        BoardColumn column = new BoardColumn();
        column.setBoard(new Board());
        card.setColumn(column);
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class, () -> attachmentService.upload(2L, file, 1L));
    }

    @Test
    void upload_tooLarge_shouldThrow() {
        byte[] bytes = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "large.txt", "text/plain", bytes);
        Card card = new Card();
        BoardColumn column = new BoardColumn();
        column.setBoard(new Board());
        card.setColumn(column);
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class, () -> attachmentService.upload(2L, file, 1L));
    }

    @Test
    void upload_cardNotFound_shouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> attachmentService.upload(2L, file, 1L));
    }

    @Test
    void delete_shouldSuccess() throws IOException {
        User user = new User();
        user.setId(1L);
        user.setFullName("User");
        Board board = new Board();
        board.setId(10L);
        BoardColumn column = new BoardColumn();
        column.setBoard(board);
        Card card = new Card();
        card.setId(2L);
        card.setColumn(column);
        Attachment attachment = Attachment.builder()
                .id(1L)
                .card(card)
                .storedName("test_stored.txt")
                .build();

        // Create dummy file
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        Files.write(Paths.get(UPLOAD_DIR, "test_stored.txt"), "data".getBytes());

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        attachmentService.delete(1L, 1L);

        verify(attachmentRepository).delete(attachment);
        assertFalse(Files.exists(Paths.get(UPLOAD_DIR, "test_stored.txt")));
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attachmentService.delete(1L, 1L));
    }

    @Test
    void getAttachments_shouldReturnList() {
        when(attachmentRepository.findByCardIdOrderByUploadedAtDesc(1L)).thenReturn(Collections.emptyList());
        assertTrue(attachmentService.getAttachments(1L).isEmpty());
    }

    @Test
    void getAttachment_shouldReturnEntity() {
        Attachment attachment = new Attachment();
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));
        assertEquals(attachment, attachmentService.getAttachment(1L));
    }

    @Test
    void getAttachment_notFound_shouldThrow() {
        when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attachmentService.getAttachment(1L));
    }

    @Test
    void broadcast_shouldHandleException() {
        doThrow(new RuntimeException("WS Error")).when(webSocketNotificationService).broadcastBoardEvent(any(), any(), any(), any(), any());
        
        // delete calls broadcastSafely
        User user = new User();
        user.setId(1L);
        user.setFullName("User");
        Board board = new Board();
        board.setId(10L);
        BoardColumn column = new BoardColumn();
        column.setBoard(board);
        Card card = new Card();
        card.setId(2L);
        card.setColumn(column);
        Attachment attachment = Attachment.builder().id(1L).card(card).storedName("s").build();
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        attachmentService.delete(1L, 1L);

        verify(webSocketNotificationService).broadcastBoardEvent(any(), any(), any(), any(), any());
        // Should not throw
    }
}
