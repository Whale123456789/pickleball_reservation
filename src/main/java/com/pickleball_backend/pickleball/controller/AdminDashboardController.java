package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.AdminBookingDto;
import com.pickleball_backend.pickleball.dto.AdminUserDto;
import com.pickleball_backend.pickleball.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminDashboardService.getAllUsers());
    }

    // Get total user count
    @GetMapping("/users/count")
    public ResponseEntity<Long> getTotalUsers() {
        return ResponseEntity.ok(adminDashboardService.getTotalUserCount());
    }

    // Get all bookings
    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingDto>> getAllBookings() {
        return ResponseEntity.ok(adminDashboardService.getAllBookings());
    }
}