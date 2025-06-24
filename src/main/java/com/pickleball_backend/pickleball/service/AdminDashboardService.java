package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.AdminBookingDto;
import com.pickleball_backend.pickleball.dto.AdminUserDto;
import java.util.List;

public interface AdminDashboardService {
    List<AdminUserDto> getAllUsers();
    long getTotalUserCount();
    List<AdminBookingDto> getAllBookings();
}