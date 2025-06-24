package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "court")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String location;
    private String status;

    @Column(name = "opening_time")
    private String openingTime;

    @Column(name = "closing_time")
    private String closingTime;

    @Column(name = "operating_days")
    private String operatingDays;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "archive_timestamp")
    private LocalDateTime archiveTimestamp;

    @Column(name = "peak_hourly_price")
    private Double peakHourlyPrice;

    @Column(name = "off_peak_hourly_price")
    private Double offPeakHourlyPrice;

    @Column(name = "daily_price")
    private Double dailyPrice;

    @Column(name = "peak_start_time")
    private String peakStartTime;

    @Column(name = "peak_end_time")
    private String peakEndTime;

    @Column(name = "number_of_courts")
    private Integer numberOfCourts = 1;
}