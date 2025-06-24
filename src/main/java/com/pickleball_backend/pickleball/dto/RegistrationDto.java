package com.pickleball_backend.pickleball.dto;

import lombok.*;

@Data
public class RegistrationDto {
    private String username;
    private String name;
    private String gender;
    private String userType;
    private String dob;
    private String email;
    private String phone;
    private String password;
    private String position;
}