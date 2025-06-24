package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.CourtDto;
import com.pickleball_backend.pickleball.dto.CourtPricingDto;
import com.pickleball_backend.pickleball.dto.SlotDto;
import com.pickleball_backend.pickleball.entity.Court;
import com.pickleball_backend.pickleball.exception.ValidationException;
import com.pickleball_backend.pickleball.repository.CourtRepository;
import com.pickleball_backend.pickleball.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.time.DayOfWeek;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {
    private final CourtRepository courtRepository;
    private final SlotService slotService;

    @Override
    public Court createCourt(CourtDto courtDto) {
        if (courtRepository.existsByNameAndLocation(courtDto.getName(), courtDto.getLocation())) {
            throw new IllegalArgumentException("Court with the same name and location already exists");
        }

        Court court = saveOrUpdateCourt(new Court(), courtDto);
        generateSlotsForNewCourt(court);
        return court;
    }

    private void generateSlotsForNewCourt(Court court) {
        try {
            if (court.getOpeningTime() == null || court.getClosingTime() == null) {
                throw new ValidationException("Court operating hours not defined");
            }

            List<SlotDto> slots = new ArrayList<>();
            LocalDate start = LocalDate.now();
            LocalDate end = start.plusMonths(3);

            LocalTime opening = LocalTime.parse(court.getOpeningTime());
            LocalTime closing = LocalTime.parse(court.getClosingTime());

            if (opening.isAfter(closing)) {
                throw new ValidationException("Opening time must be before closing time");
            }

            Set<DayOfWeek> operatingDaySet = parseOperatingDays(court.getOperatingDays());

            for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
                if (!operatingDaySet.isEmpty() && !operatingDaySet.contains(date.getDayOfWeek())) {
                    continue;
                }

                LocalTime slotStart = opening;
                while (slotStart.isBefore(closing)) {
                    SlotDto slot = new SlotDto();
                    slot.setCourtId(court.getId());
                    slot.setDate(date);
                    slot.setStartTime(slotStart);

                    LocalTime slotEnd = slotStart.plusHours(1);
                    if (slotEnd.isAfter(closing)) {
                        slotEnd = closing;
                    }

                    slot.setEndTime(slotEnd);
                    slot.setAvailable(true);
                    slots.add(slot);
                    slotStart = slotEnd;
                }
            }
            slotService.createSlots(slots);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid time format: " + e.getMessage());
        }
    }

    private Set<DayOfWeek> parseOperatingDays(String operatingDaysStr) {
        if (operatingDaysStr == null || operatingDaysStr.trim().isEmpty()) {
            return EnumSet.allOf(DayOfWeek.class);
        }
        return Arrays.stream(operatingDaysStr.split(","))
                .map(String::trim)
                .map(this::parseDayOfWeek)
                .collect(Collectors.toSet());
    }

    private DayOfWeek parseDayOfWeek(String dayStr) {
        try {
            return DayOfWeek.valueOf(dayStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid day in operating days: " + dayStr);
        }
    }

    @Override
    @Transactional
    public Court updateCourt(Integer id, CourtDto courtDto) {
        Court existingCourt = courtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Court not found with id: " + id));

        // Check for duplicate name/location only if they're being changed
        if (!existingCourt.getName().equals(courtDto.getName()) ||
                !existingCourt.getLocation().equals(courtDto.getLocation())) {

            if (courtRepository.existsByNameAndLocation(courtDto.getName(), courtDto.getLocation())) {
                throw new IllegalArgumentException("Another court with the same name and location already exists");
            }
        }

        return saveOrUpdateCourt(existingCourt, courtDto);
    }

    private Court saveOrUpdateCourt(Court court, CourtDto courtDto) {
        court.setName(courtDto.getName());
        court.setLocation(courtDto.getLocation());
        court.setStatus(courtDto.getStatus());
        court.setOpeningTime(courtDto.getOpeningTime());
        court.setClosingTime(courtDto.getClosingTime());
        court.setOperatingDays(courtDto.getOperatingDays());
        court.setPeakHourlyPrice(courtDto.getPeakHourlyPrice());
        court.setOffPeakHourlyPrice(courtDto.getOffPeakHourlyPrice());
        court.setDailyPrice(courtDto.getDailyPrice());
        court.setPeakStartTime(courtDto.getPeakStartTime());
        court.setPeakEndTime(courtDto.getPeakEndTime());

        validatePeakTimes(courtDto);
        return courtRepository.save(court);
    }

    private void validatePeakTimes(CourtDto courtDto) {
        if (courtDto.getPeakStartTime() != null && courtDto.getPeakEndTime() != null) {
            LocalTime start = LocalTime.parse(courtDto.getPeakStartTime());
            LocalTime end = LocalTime.parse(courtDto.getPeakEndTime());

            if (!start.isBefore(end)) {
                throw new IllegalArgumentException("Peak start time must be before end time");
            }

            // Check against operating hours
            if (courtDto.getOpeningTime() != null && courtDto.getClosingTime() != null) {
                LocalTime opening = LocalTime.parse(courtDto.getOpeningTime());
                LocalTime closing = LocalTime.parse(courtDto.getClosingTime());

                if (start.isBefore(opening) || end.isAfter(closing)) {
                    throw new IllegalArgumentException(
                            "Peak hours must be within operating hours"
                    );
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteCourt(Integer id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Court not found with id: " + id));

        // Handle null isArchived case
        if (court.getIsArchived() != null && court.getIsArchived()) {
            throw new IllegalStateException("Court already deleted");
        }

        if (hasActiveBookings(id)) {
            throw new IllegalStateException("Cannot delete court with active bookings");
        }

        courtRepository.softDeleteCourt(id, LocalDateTime.now());
    }

    private boolean hasActiveBookings(Integer courtId) {
        // Temporary implementation
        return false;
    }

    @Override
    @Transactional
    public void updateCourtPricing(Integer id, CourtPricingDto pricingDto) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Court not found with id: " + id));

        // Validate peak times
        if (pricingDto.getPeakStartTime() != null && pricingDto.getPeakEndTime() != null) {
            LocalTime start = LocalTime.parse(pricingDto.getPeakStartTime());
            LocalTime end = LocalTime.parse(pricingDto.getPeakEndTime());

            if (!start.isBefore(end)) {
                throw new IllegalArgumentException("Peak start time must be before end time");
            }

            // Check against operating hours only if they exist
            if (court.getOpeningTime() != null && court.getClosingTime() != null) {
                try {
                    LocalTime opening = LocalTime.parse(court.getOpeningTime());
                    LocalTime closing = LocalTime.parse(court.getClosingTime());

                    if (start.isBefore(opening) || end.isAfter(closing)) {
                        throw new IllegalArgumentException(
                                "Peak hours must be within operating hours"
                        );
                    }
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid operating hours format");
                }
            }
        }

        // Update pricing fields
        court.setPeakHourlyPrice(pricingDto.getPeakHourlyPrice());
        court.setOffPeakHourlyPrice(pricingDto.getOffPeakHourlyPrice());
        court.setDailyPrice(pricingDto.getDailyPrice());
        court.setPeakStartTime(pricingDto.getPeakStartTime());
        court.setPeakEndTime(pricingDto.getPeakEndTime());

        courtRepository.save(court);
    }

    //slot
    @Override
    public List<Court> getAllCourts() {
        return courtRepository.findActiveCourts(); // Use the new query
    }


    @Override
    public Court getCourtByIdForMember(Integer id) {
        return courtRepository.findById(id)
                .filter(court ->
                        court.getIsArchived() == null ||
                                !court.getIsArchived()
                )
                .orElse(null);
    }




}