package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.CourtDto;
import com.pickleball_backend.pickleball.dto.CourtPricingDto;
import com.pickleball_backend.pickleball.entity.Court;

import java.util.List;

public interface CourtService {
    Court createCourt(CourtDto courtDto);
    Court updateCourt(Integer id, CourtDto courtDto);
    void deleteCourt(Integer id);
    void updateCourtPricing(Integer id, CourtPricingDto pricingDto);
    List<Court> getAllCourts();
    Court getCourtByIdForMember(Integer id);
}