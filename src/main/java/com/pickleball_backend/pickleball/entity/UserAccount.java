package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "useraccount")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String password;
    private String status = "ACTIVE";
    private int failedLoginAttempts = 0;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLogin;
    private LocalDateTime passwordChangedAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;
    @Column(name = "profile_image", length = 255, nullable = true)
    private String profileImage;
    private String theme = "light";
    private boolean emailNotifications = true;
    private boolean pushNotifications = true;

    // FIXED RELATIONSHIP: Changed to OneToOne
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
}