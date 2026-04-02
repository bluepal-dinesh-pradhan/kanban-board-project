package com.example.demo.service;

import com.example.demo.dto.ActivityDto;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Activity;
import com.example.demo.entity.Board;
import com.example.demo.entity.User;
import com.example.demo.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void log_shouldSuccess() {
        Board board = new Board();
        User user = new User();
        user.setId(1L);
        activityService.log(board, user, "action", "type", 1L);
        verify(activityRepository).save(any());
    }

    @Test
    void getByBoard_shouldReturnList() {
        when(activityRepository.findByBoardIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        List<ActivityDto> result = activityService.getByBoard(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getByBoard_paged_shouldReturnPage() {
        Page<Activity> page = new PageImpl<>(Collections.emptyList());
        when(activityRepository.findByBoardIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class))).thenReturn(page);
        PageResponse<ActivityDto> result = activityService.getByBoard(1L, 0, 10);
        assertTrue(result.getContent().isEmpty());
    }
}
