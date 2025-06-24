package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.SlotDto;
import com.pickleball_backend.pickleball.dto.SlotResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface SlotService {
    List<SlotResponseDto> getSlots(List<Integer> courtIds, LocalDate startDate, LocalDate endDate);
    void createSlots(List<SlotDto> slotDtos);
    List<SlotResponseDto> getAvailableSlotsByCourt(Integer courtId);

}