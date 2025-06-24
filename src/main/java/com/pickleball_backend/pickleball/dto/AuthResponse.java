package com.pickleball_backend.pickleball.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
}