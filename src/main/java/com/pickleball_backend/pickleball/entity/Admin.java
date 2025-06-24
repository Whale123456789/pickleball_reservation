package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "admin")
public class Admin {
    @Id
    private Integer id;  // Will be set by @MapsId

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId  // Automatically sets ID from user ID
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "position")
    private String position;
}