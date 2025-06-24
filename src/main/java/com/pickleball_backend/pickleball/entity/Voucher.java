package com.pickleball_backend.pickleball.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "voucher")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties("tier")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String code;

    // Correctly mapped to snake_case columns
    @Column(name = "discount_amount", nullable = false)
    private double discountAmount;

    @Column(name = "request_points", nullable = false)
    private int requestPoints;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(name = "tier_id", nullable = false)
    @JsonBackReference
    private MembershipTier tier;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}