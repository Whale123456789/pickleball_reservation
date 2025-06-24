package com.pickleball_backend.pickleball.dto;

import com.pickleball_backend.pickleball.entity.Feedback.TargetType;
import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
public class FeedbackDto {
    @NotNull(message = "Target type is required")
    private TargetType targetType;

    @NotNull(message = "Target ID is required")
    private Integer targetId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String review;
    private List<String> tags;
}