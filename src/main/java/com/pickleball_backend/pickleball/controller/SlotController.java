package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.SlotResponseDto;
import com.pickleball_backend.pickleball.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/member/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getSlots(
            @RequestParam(name = "courtids", required = false) List<Integer> courtIds,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Validation - both dates required
        if (startDate == null || endDate == null) {
            return ResponseEntity.badRequest().body("Both startDate and endDate are required");
        }

        // Validation - start date must be before end date
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("Start date must be before end date");
        }

        // Service call - already implemented correctly
        List<SlotResponseDto> slots = slotService.getSlots(courtIds, startDate, endDate);

        return !slots.isEmpty()
                ? ResponseEntity.ok(slots)
                : ResponseEntity.ok("No slots found for selected date range/courts");
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SlotResponseDto>> getAvailableSlots(
            @RequestParam Integer courtId) {

        List<SlotResponseDto> slots = slotService.getAvailableSlotsByCourt(courtId);
        return ResponseEntity.ok(slots);
    }
}