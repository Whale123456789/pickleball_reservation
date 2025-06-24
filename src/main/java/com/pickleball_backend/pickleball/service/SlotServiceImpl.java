package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.SlotDto;
import com.pickleball_backend.pickleball.dto.SlotResponseDto;
import com.pickleball_backend.pickleball.entity.Court;
import com.pickleball_backend.pickleball.entity.Slot;
import com.pickleball_backend.pickleball.exception.ValidationException;
import com.pickleball_backend.pickleball.repository.CourtRepository;
import com.pickleball_backend.pickleball.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private final SlotRepository slotRepository;
    private final CourtRepository courtRepository;

    @Override
    public List<SlotResponseDto> getSlots(List<Integer> courtIds, LocalDate startDate, LocalDate endDate) {
        List<Slot> slots;

        if (courtIds == null || courtIds.isEmpty()) {
            slots = slotRepository.findByDateBetween(startDate, endDate);
        } else {
            slots = new ArrayList<>();
            for (Integer courtId : courtIds) {
                slots.addAll(slotRepository.findByCourtIdAndDateBetween(courtId, startDate, endDate));
            }
        }

        if (slots.isEmpty()) {
            return Collections.emptyList();
        }

        // Get court details in bulk
        Set<Integer> courtIdsInSlots = slots.stream()
                .map(Slot::getCourtId)
                .collect(Collectors.toSet());

        Map<Integer, Court> courts = courtIdsInSlots.isEmpty()
                ? Collections.emptyMap()
                : courtRepository.findAllById(courtIdsInSlots).stream()
                .collect(Collectors.toMap(Court::getId, court -> court));

        return slots.stream().map(slot -> {
            SlotResponseDto dto = new SlotResponseDto();
            dto.setId(slot.getId());
            dto.setCourtId(slot.getCourtId());
            dto.setDate(slot.getDate()); // Sets both date string and dayOfWeek
            dto.setStartTime(slot.getStartTime());
            dto.setEndTime(slot.getEndTime());

            Court court = courts.get(slot.getCourtId());
            if (court != null) {
                dto.setCourtName(court.getName());
                dto.setCourtLocation(court.getLocation());

                // Enhanced status calculation
                dto.setStatus(determineSlotStatus(slot, court));
            } else {
                dto.setStatus("UNKNOWN");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    private String determineSlotStatus(Slot slot, Court court) {
        if (!slot.isAvailable()) {
            return "BOOKED";
        }
        if ("MAINTENANCE".equals(court.getStatus())) {
            return "MAINTENANCE";
        }
        if (!isOperatingDay(slot, court)) {
            return "CLOSED";
        }
        if (!isDuringOperatingHours(slot, court)) {
            return "CLOSED";
        }
        return "AVAILABLE";
    }

    private boolean isOperatingDay(Slot slot, Court court) {
        if (court.getOperatingDays() == null) return false;

        DayOfWeek slotDay = slot.getDate().getDayOfWeek();
        String[] operatingDays = court.getOperatingDays().split(",");

        for (String day : operatingDays) {
            try {
                DayOfWeek courtDay = DayOfWeek.valueOf(day.trim().toUpperCase());
                if (courtDay == slotDay) {
                    return true;
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid day string - skip
            }
        }
        return false;
    }

    private boolean isDuringOperatingHours(Slot slot, Court court) {
        try {
            LocalTime opening = LocalTime.parse(court.getOpeningTime());
            LocalTime closing = LocalTime.parse(court.getClosingTime());
            return !slot.getStartTime().isBefore(opening) &&
                    !slot.getEndTime().isAfter(closing);
        } catch (Exception e) {
            return false;
        }
    }



    @Override
    @Transactional
    public void createSlots(List<SlotDto> slotDtos) {
        slotDtos.forEach(dto -> {
            // Add null checks for critical fields
            if (dto.getStartTime() == null) {
                throw new ValidationException("Start time is required for slot creation");
            }
            if (dto.getEndTime() == null) {
                throw new ValidationException("End time is required for slot creation");
            }

            Slot slot = new Slot();
            slot.setCourtId(dto.getCourtId());
            slot.setDate(dto.getDate());
            slot.setStartTime(dto.getStartTime());  // Must not be null
            slot.setEndTime(dto.getEndTime());      // Must not be null
            slot.setAvailable(dto.isAvailable());
            slotRepository.save(slot);
        });
    }

    @Override
    public List<SlotResponseDto> getAvailableSlotsByCourt(Integer courtId) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7); // Next 7 days

        List<Slot> slots = slotRepository.findByCourtIdAndDateBetweenAndIsAvailableTrue(
                courtId, today, endDate);

        return slots.stream().map(slot -> {
            SlotResponseDto dto = new SlotResponseDto();
            dto.setId(slot.getId());
            dto.setCourtId(slot.getCourtId());
            dto.setDate(slot.getDate());
            dto.setStartTime(slot.getStartTime());
            dto.setEndTime(slot.getEndTime());
            dto.setStatus("AVAILABLE");
            return dto;
        }).collect(Collectors.toList());
    }
}