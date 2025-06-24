package com.pickleball_backend.pickleball.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRedemptionResponse {
    private String voucherCode;
    private LocalDate expiryDate;
    private int remainingPoints;
}