package com.pickleball_backend.pickleball.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PendingCancellationRequestDto {
    private Integer requestId;
    private Integer bookingId;
    private String memberName;
    private LocalDate date;
    private LocalTime startTime;
    private String courtName;
    private String reason;
}