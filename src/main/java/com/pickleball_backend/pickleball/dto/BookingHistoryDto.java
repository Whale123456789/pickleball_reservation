package com.pickleball_backend.pickleball.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingHistoryDto {
    private Integer id;
    private String courtName;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double amount;
    private String status; // UPCOMING, COMPLETED, CANCELLED
    private LocalDate createdAt;
    private String purpose;
    private Integer players;
    private Integer courtNumber;
    private Integer durationHours;
}