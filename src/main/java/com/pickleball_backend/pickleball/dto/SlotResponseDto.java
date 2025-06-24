package com.pickleball_backend.pickleball.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SlotResponseDto {
    private Integer id;
    private Integer courtId;
    private String courtName;
    private String courtLocation;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Integer courtNumber;
    private Integer durationHours;
}