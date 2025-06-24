package com.pickleball_backend.pickleball.dto;

import lombok.Data;

@Data
public class ReviewableItemDto {
    private String type; // "court", "event", "coach"
    private Integer id;
    private String name;
}