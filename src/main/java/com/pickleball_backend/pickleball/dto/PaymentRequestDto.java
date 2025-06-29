package com.pickleball_backend.pickleball.dto;

import lombok.Data;

@Data
public class PaymentRequestDto {
    private BookingRequestDto bookingRequest;
    private boolean generateReceipt;
}