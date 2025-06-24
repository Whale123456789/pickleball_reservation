package com.pickleball_backend.pickleball.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AverageRatingDto {
    private Double averageRating;
    private Integer totalReviews;
}