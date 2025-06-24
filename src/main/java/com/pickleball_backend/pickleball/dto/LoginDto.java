package com.pickleball_backend.pickleball.dto;

import lombok.*;

@Data
public class LoginDto {
    private String usernameOrEmail;
    private String password;
}
