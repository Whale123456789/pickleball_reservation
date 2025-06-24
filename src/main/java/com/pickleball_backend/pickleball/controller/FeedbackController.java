// FeedbackController.java
package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.*;
import com.pickleball_backend.pickleball.entity.Feedback;
import com.pickleball_backend.pickleball.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponseDto> createFeedback(@RequestBody FeedbackDto dto) {
        return ResponseEntity.ok(feedbackService.createFeedback(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponseDto> updateFeedback(
            @PathVariable Integer id,
            @RequestBody FeedbackDto dto
    ) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponseDto>> getFeedbackForTarget(
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Integer targetId
    ) {
        return ResponseEntity.ok(feedbackService.getFeedbackForTarget(
                Feedback.TargetType.valueOf(targetType.toUpperCase()),
                targetId
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<FeedbackStatsDto> getFeedbackStats(
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Integer targetId
    ) {
        return ResponseEntity.ok(feedbackService.getFeedbackStats(
                Feedback.TargetType.valueOf(targetType.toUpperCase()),
                targetId
        ));
    }
}