package com.pickleball_backend.pickleball.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private String username;
    private String name;
    private String email;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String userType;
    private String requestedUserType;
    private String status;
    private String profileImage;
    private int bookingsMade;
    private double bookingHours;
    private int sumosJoined;
    private double amountSpent;
    private String theme;
    private boolean emailNotifications;
    private boolean pushNotifications;
}