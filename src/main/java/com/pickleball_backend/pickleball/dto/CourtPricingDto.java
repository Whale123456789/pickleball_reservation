package com.pickleball_backend.pickleball.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CourtPricingDto {
    @PositiveOrZero(message = "Peak hourly price must be positive or zero")
    private Double peakHourlyPrice;

    @PositiveOrZero(message = "Off-peak hourly price must be positive or zero")
    private Double offPeakHourlyPrice;

    @PositiveOrZero(message = "Daily price must be positive or zero")
    private Double dailyPrice;

    @Pattern(regexp = "^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$",
            message = "Invalid peak start time format (HH:mm)")
    private String peakStartTime;

    @Pattern(regexp = "^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$",
            message = "Invalid peak end time format (HH:mm)")
    private String peakEndTime;
}