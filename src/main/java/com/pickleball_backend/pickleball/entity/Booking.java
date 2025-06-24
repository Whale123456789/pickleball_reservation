package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "total_amount", nullable = false, columnDefinition = "double(5,2)")
    private double totalAmount;

    @Column( length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, referencedColumnName = "user_id")
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "number_of_players")
    private Integer numberOfPlayers;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private BookingSlot bookingSlot;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private CancellationRequest cancellationRequest;

    public void setBookingSlot(BookingSlot bookingSlot) {
        if (bookingSlot == null) {
            if (this.bookingSlot != null) {
                this.bookingSlot.setBooking(null);
            }
        } else {
            bookingSlot.setBooking(this);
        }
        this.bookingSlot = bookingSlot;
    }

    public enum status {
        CONFIRMED, CANCELLED, PENDING
    }

}