package com.pickleball_backend.pickleball.dto;

import lombok.Data;

@Data
public class BookingRequestDto {
    private Integer slotId;
    private String purpose;
    private Integer numberOfPlayers;
    private Integer durationHours;
    private boolean useWallet;
    private String paymentMethod;
    private String paymentStatus;
}