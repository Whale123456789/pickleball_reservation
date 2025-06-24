package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.CourtDto;
import com.pickleball_backend.pickleball.dto.CourtPricingDto;
import com.pickleball_backend.pickleball.entity.Court;
import com.pickleball_backend.pickleball.service.CourtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/courts")
@RequiredArgsConstructor
public class CourtController {
    private final CourtService courtService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createCourt(@RequestBody CourtDto courtDto) {
        try {
            Court newCourt = courtService.createCourt(courtDto);
            return new ResponseEntity<>(newCourt, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating court");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateCourt(
            @PathVariable Integer id,
            @RequestBody CourtDto courtDto) {
        try {
            Court updatedCourt = courtService.updateCourt(id, courtDto);
            return new ResponseEntity<>(updatedCourt, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating court");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCourt(@PathVariable Integer id) {
        try {
            courtService.deleteCourt(id);
            return ResponseEntity.ok("Court archived successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error archiving court");
        }
    }

    @PutMapping("/{id}/pricing")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateCourtPricing(
            @PathVariable Integer id,
            @RequestBody @Valid CourtPricingDto pricingDto) {
        try {
            courtService.updateCourtPricing(id, pricingDto);
            return ResponseEntity.ok("Pricing updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating pricing");
        }
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getCourtAnalytics(@PathVariable Integer id) {
        try {
            // TODO: Implement analytics service
            Map<String, Object> analytics = Map.of(
                    "bookingsLastMonth", 42,
                    "peakHours", Arrays.asList("18:00-20:00", "20:00-22:00"),
                    "revenue", 1250.00
            );
            return new ResponseEntity<>(analytics, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching analytics");
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllCourts() {
        try {
            List<Court> courts = courtService.getAllCourts();
            if (courts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body("No courts have been added yet.");
            }
            return new ResponseEntity<>(courts, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving courts");
        }
    }
}