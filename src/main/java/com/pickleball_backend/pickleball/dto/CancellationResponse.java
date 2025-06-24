package com.pickleball_backend.pickleball.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CancellationResponse {
    private Integer requestId;
    private Integer bookingId;
    private String status;
    private LocalDate requestDate;
    private String message;


}