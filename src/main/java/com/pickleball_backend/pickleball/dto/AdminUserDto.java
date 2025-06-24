package com.pickleball_backend.pickleball.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AdminUserDto {
    private Integer id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String userType;
    private String status;
    private String profileImage;
    private int pointBalance;
    private String tier;
}