package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "slot")
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "court_id")
    private Integer courtId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_available")
    private boolean isAvailable;

    @OneToOne(mappedBy = "slot", cascade = CascadeType.ALL)
    private BookingSlot bookingSlot;

    @Column(name = "status")
    private String status;

    @Column(name = "court_number")
    private Integer courtNumber;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours = 1;
}