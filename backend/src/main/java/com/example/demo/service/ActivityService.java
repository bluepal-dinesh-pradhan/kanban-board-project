package com.example.demo.service;

import com.example.demo.dto.ActivityDto;
import com.example.demo.entity.Activity;
import com.example.demo.entity.Board;
import com.example.demo.entity.User;
import com.example.demo.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public void log(Board board, User user, String action, String entityType, Long entityId) {
        Activity a = Activity.builder()
                .board(board).user(user).action(action)
                .entityType(entityType).entityId(entityId)
                .build();
        activityRepository.save(a);
    }

    public List<ActivityDto> getByBoard(Long boardId) {
        return activityRepository.findTop50ByBoardIdOrderByCreatedAtDesc(boardId)
                .stream().map(ActivityDto::from).collect(Collectors.toList());
    }
}
