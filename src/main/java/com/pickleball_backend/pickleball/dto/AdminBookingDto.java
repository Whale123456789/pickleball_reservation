package com.pickleball_backend.pickleball.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AdminBookingDto {
    private Integer id;
    private LocalDate bookingDate;
    private double totalAmount;
    private String status;
    private String memberName;
    private String courtName;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private Integer numberOfPlayers;
}