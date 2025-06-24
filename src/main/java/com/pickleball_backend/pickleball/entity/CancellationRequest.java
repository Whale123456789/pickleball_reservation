package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "cancellationrequest")
public class CancellationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    private String reason;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false, length = 50)
    private String status; // PENDING, APPROVED, REJECTED
}