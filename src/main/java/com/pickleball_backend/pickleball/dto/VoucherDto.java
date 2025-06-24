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
public class VoucherDto {
    private Integer id;
    private String code;
    private double discountAmount;
    private int requestPoints;
    private LocalDate expiryDate;
}