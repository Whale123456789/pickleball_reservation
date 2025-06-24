package com.pickleball_backend.pickleball.dto;

import lombok.Data;

@Data
public class BookingRequestDto {
    private Integer slotId;
    private String purpose;
    private Integer numberOfPlayers;
    private Integer durationHours;
}