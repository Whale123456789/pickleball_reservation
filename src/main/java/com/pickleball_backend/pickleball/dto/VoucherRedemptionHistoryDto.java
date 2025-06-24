// VoucherRedemptionHistoryDto.java
package com.pickleball_backend.pickleball.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoucherRedemptionHistoryDto {
    private int id;
    private String voucherCode;
    private double discountAmount;
    private int requestPoints;
    private LocalDate redemptionDate;
    private LocalDate expiryDate;

}