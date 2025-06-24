package com.pickleball_backend.pickleball.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AdminRegistrationDTO {
    private String name;
    private String gender;
    private String email;
    private String phone;
    private LocalDate dob;
    private String username;
    private String password;
    private String position;
}