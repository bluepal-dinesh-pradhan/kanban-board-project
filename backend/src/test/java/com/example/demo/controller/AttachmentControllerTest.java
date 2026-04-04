package com.example.demo.controller;

import com.example.demo.dto.AttachmentDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtService;
import com.example.demo.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

    @Autowired
    private CardRepository cardRepository;

    @MockBean
    private AttachmentService attachmentService;

    @Autowired
    private JwtService jwtService;

    private String token;
    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("attachment@example.com")
                .password("password")
                .fullName("Attachment User")
                .build());

        Board board = boardRepository.save(Board.builder()
                .title("Attachment Board")
                .owner(user)
                .build());

        boardMemberRepository.save(BoardMember.builder()
                .board(board)
                .user(user)
                .role(BoardMember.Role.OWNER)
                .build());

        BoardColumn column = boardColumnRepository.save(BoardColumn.builder()
                .title("Column")
                .board(board)
                .position(0)
                .build());

        card = cardRepository.save(Card.builder()
                .title("Card")
                .column(column)
                .position(0)
                .build());

        token = "Bearer " + jwtService.generateAccessToken(user.getId(), user.getEmail());
    }

    @Test
    void upload_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        
        AttachmentDto dto = new AttachmentDto();
        dto.setId(1L);
        dto.setFileName("test.txt");
        
        when(attachmentService.upload(eq(card.getId()), any(), eq(user.getId()))).thenReturn(dto);

        mockMvc.perform(multipart("/api/cards/" + card.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileName").value("test.txt"));
    }

    @Test
    void getAttachments_returns200() throws Exception {
        AttachmentDto dto = new AttachmentDto();
        dto.setId(1L);
        dto.setFileName("test.txt");

        when(attachmentService.getAttachments(card.getId())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/cards/" + card.getId() + "/attachments")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fileName").value("test.txt"));
    }

    @Test
    void delete_returns200() throws Exception {
        mockMvc.perform(delete("/api/attachments/1")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void download_returns200() throws Exception {
        // Need to return a real Attachment for download to work if the service is not mocked for this call
        // But since download() calls attachmentService.getAttachment(id) and attachmentService.getFilePath(attachment)
        // I should mock them too.
        
        Attachment attachment = Attachment.builder()
                .id(1L)
                .fileName("test.txt")
                .storedName("stored.txt")
                .fileType("text/plain")
                .build();
        
        when(attachmentService.getAttachment(1L)).thenReturn(attachment);
        // Note: download() might still fail if the file doesn't exist on disk because UrlResource(filePath.toUri()) is used.
        // However, I can mock getFilePath to return a path to a real temporary file.
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".txt");
        java.nio.file.Files.writeString(tempFile, "content");
        
        when(attachmentService.getFilePath(any())).thenReturn(tempFile);

        mockMvc.perform(get("/api/attachments/1/download")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                .andExpect(content().string("content"));
    }

    @Test
    void uploadWithoutAuth_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        mockMvc.perform(multipart("/api/cards/" + card.getId() + "/attachments").file(file))
                .andExpect(status().isUnauthorized());
    }
}
