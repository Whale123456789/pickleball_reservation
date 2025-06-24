package com.pickleball_backend.pickleball.dto;

import lombok.Data;

@Data
public class CourtDto {
    private String name;
    private String location;
    private String status;
    private String openingTime;
    private String closingTime;
    private String operatingDays;
    private Double peakHourlyPrice;
    private Double offPeakHourlyPrice;
    private Double dailyPrice;
    private String peakStartTime;
    private String peakEndTime;
    private Integer numberOfCourts = 1;
}