package com.pickleball_backend.pickleball.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserTypeChangeResponse {
    private Integer userId;
    private String userName;
    private String currentType;
    private String requestedType;
}