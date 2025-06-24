package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private UUID uuid = UUID.randomUUID();

    private String name;
    private String gender;
    private String email;
    private String phone;

    private LocalDate dob;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "requested_user_type") // New field for type change requests
    private String requestedUserType;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "bookings_made")
    private int bookingsMade;

    @Column(name = "booking_hours")
    private double bookingHours;

    @Column(name = "sumos_joined")
    private int sumosJoined;

    @Column(name = "amount_spent")
    private double amountSpent;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Member member;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserAccount userAccount;
}