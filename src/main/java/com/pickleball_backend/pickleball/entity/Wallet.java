package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 100.00")
    private double balance;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}