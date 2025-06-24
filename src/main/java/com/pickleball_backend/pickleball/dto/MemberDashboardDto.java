package com.pickleball_backend.pickleball.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDashboardDto {
    private String tierName;
    private int pointBalance;
    private String benefits;
    private List<VoucherDto> redeemableVouchers;
    private int minPoints;  // Add this
    private int maxPoints;  // Add this
}