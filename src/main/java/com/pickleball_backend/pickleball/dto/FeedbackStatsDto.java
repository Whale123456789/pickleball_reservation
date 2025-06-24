package com.pickleball_backend.pickleball.dto;

import lombok.Data;

@Data
public class FeedbackStatsDto {
    private double averageRating;
    private int totalReviews;
}