// FeedbackService.java
package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.*;
import com.pickleball_backend.pickleball.entity.*;
import com.pickleball_backend.pickleball.exception.ResourceNotFoundException;
import com.pickleball_backend.pickleball.exception.ValidationException;
import com.pickleball_backend.pickleball.repository.FeedbackRepository;
import com.pickleball_backend.pickleball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackResponseDto createFeedback(FeedbackDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUserAccount_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Feedback feedback = new Feedback();
        feedback.setTargetType(dto.getTargetType());
        feedback.setTargetId(dto.getTargetId());
        feedback.setRating(dto.getRating());
        feedback.setReview(dto.getReview());
        feedback.setUser(user);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setTags(dto.getTags());

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return convertToDto(savedFeedback);
    }

    public FeedbackResponseDto updateFeedback(Integer id, FeedbackDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));

        // Verify user owns the feedback
        if (!feedback.getUser().getUserAccount().getUsername().equals(username)) {
            throw new ValidationException("You can only update your own feedback");
        }

        feedback.setRating(dto.getRating());
        feedback.setReview(dto.getReview());

        Feedback updatedFeedback = feedbackRepository.save(feedback);
        return convertToDto(updatedFeedback);
    }

    public void deleteFeedback(Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));

        // Verify user owns the feedback or is admin
        boolean isOwner = feedback.getUser().getUserAccount().getUsername().equals(username);
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new ValidationException("You don't have permission to delete this feedback");
        }

        feedbackRepository.delete(feedback);
    }

    public List<FeedbackResponseDto> getFeedbackForTarget(
            Feedback.TargetType targetType,
            Integer targetId
    ) {
        List<Feedback> feedbackList = feedbackRepository
                .findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);

        return feedbackList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public FeedbackStatsDto getFeedbackStats(
            Feedback.TargetType targetType,
            Integer targetId
    ) {
        Double average = feedbackRepository.findAverageRatingByTarget(targetType, targetId);
        Integer count = feedbackRepository.countByTarget(targetType, targetId);

        FeedbackStatsDto stats = new FeedbackStatsDto();
        stats.setAverageRating(average != null ? average : 0.0);
        stats.setTotalReviews(count != null ? count : 0);
        return stats;
    }

    private FeedbackResponseDto convertToDto(Feedback feedback) {
        FeedbackResponseDto dto = new FeedbackResponseDto();
        dto.setId(feedback.getId());
        dto.setTargetType(feedback.getTargetType().name());
        dto.setTargetId(feedback.getTargetId());
        dto.setRating(feedback.getRating());
        dto.setReview(feedback.getReview());
        dto.setUserName(feedback.getUser().getName());
        dto.setCreatedAt(feedback.getCreatedAt());
        return dto;
    }
}