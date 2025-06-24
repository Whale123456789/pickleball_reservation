package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "feedback")
public class Feedback {

    public enum TargetType {
        COURT, EVENT, COACH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private Integer targetId; // ID of court, event, or coach

    private Integer rating; // 1-5

    private String review;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection
    private List<String> tags = new ArrayList<>();
}