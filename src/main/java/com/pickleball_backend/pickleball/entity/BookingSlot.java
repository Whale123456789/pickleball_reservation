package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "bookingslot")
public class BookingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ADDED
    private Integer id;

    @OneToOne
    @MapsId  // Shares the same ID as Booking
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false, length = 50)
    private String status; // BOOKED, CANCELLED, COMPLETED

}