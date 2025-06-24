// ResetPasswordRequest.java
package com.pickleball_backend.pickleball.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}