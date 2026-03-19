package com.example.demo.service;

import com.example.demo.dto.ActivityDto;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Activity;
import com.example.demo.entity.Board;
import com.example.demo.entity.User;
import com.example.demo.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;

    public void log(Board board, User user, String action, String entityType, Long entityId) {
        log.info("Logging activity {} for entity {}:{} by user {}", action, entityType, entityId, user.getId());
        Activity a = Activity.builder()
                .board(board).user(user).action(action)
                .entityType(entityType).entityId(entityId)
                .build();
        activityRepository.save(a);
        log.info("Activity logged successfully with id {}", a.getId());
    }

    public List<ActivityDto> getByBoard(Long boardId) {
        log.info("Fetching activities for board {}", boardId);
        List<ActivityDto> activities = activityRepository.findByBoardIdOrderByCreatedAtDesc(boardId)
                .stream().map(ActivityDto::from).collect(Collectors.toList());
        log.debug("Fetching {} activities for board {}", activities.size(), boardId);
        log.info("Activities fetched successfully for board {}", boardId);
        return activities;
    }

    public PageResponse<ActivityDto> getByBoard(Long boardId, int page, int size) {
        log.info("Fetching activities for board {} with page {} and size {}", boardId, page, size);
        Page<Activity> activities = activityRepository.findByBoardIdOrderByCreatedAtDesc(
                boardId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        PageResponse<ActivityDto> response = PageResponse.from(activities.map(ActivityDto::from));
        log.debug("Fetching {} activities for board {} (page {}, size {})", response.getContent().size(), boardId, page, size);
        log.info("Activities fetched successfully for board {} with page {} and size {}", boardId, page, size);
        return response;
    }
}
