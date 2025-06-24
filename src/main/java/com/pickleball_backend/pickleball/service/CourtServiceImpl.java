package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.CourtDto;
import com.pickleball_backend.pickleball.dto.CourtPricingDto;
import com.pickleball_backend.pickleball.dto.SlotDto;
import com.pickleball_backend.pickleball.entity.*;
import com.pickleball_backend.pickleball.exception.ValidationException;
import com.pickleball_backend.pickleball.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(CourtServiceImpl.class);
    private final CourtRepository courtRepository;
    private final SlotService slotService;
    private final BookingRepository bookingRepository;
    private final MemberRepository memberRepository;
    private final SlotRepository slotRepository;
    private final EmailService emailService;
    private final PaymentRepository paymentRepository;
    private final BookingSlotRepository bookingSlotRepository;

    @Override
    public Court createCourt(CourtDto courtDto) {
        if (courtRepository.existsByNameAndLocation(courtDto.getName(), courtDto.getLocation())) {
            throw new IllegalArgumentException("Court with the same name and location already exists");
        }

        Court mainCourt = saveOrUpdateCourt(new Court(), courtDto);
        mainCourt.setNumberOfCourts(courtDto.getNumberOfCourts());
        courtRepository.save(mainCourt);

        generateSlotsForNewCourt(mainCourt, courtDto.getNumberOfCourts());

        return mainCourt;
    }

    private void generateSlotsForNewCourt(Court court, int numberOfCourts) {
        try {
            if (court.getOpeningTime() == null || court.getClosingTime() == null) {
                throw new ValidationException("Court operating hours not defined");
            }

            LocalTime opening = LocalTime.parse(court.getOpeningTime());
            LocalTime closing = LocalTime.parse(court.getClosingTime());

            if (opening.isAfter(closing)) {
                throw new ValidationException("Opening time must be before closing time");
            }

            Set<DayOfWeek> operatingDaySet = parseOperatingDays(court.getOperatingDays());
            LocalDate start = LocalDate.now();
            LocalDate end = start.plusMonths(3);

            for (int courtNumber = 1; courtNumber <= numberOfCourts; courtNumber++) {
                List<SlotDto> slots = new ArrayList<>();

                for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
                    if (!operatingDaySet.isEmpty() && !operatingDaySet.contains(date.getDayOfWeek())) {
                        continue;
                    }
                    LocalTime slotStart = opening;

                    while (slotStart.isBefore(closing)) {
                        // Generate slots for 1, 2, and 3 hours
                        for (int hours : Arrays.asList(1, 2, 3)) {
                            LocalTime slotEnd = slotStart.plusHours(hours);

                            if (slotEnd.isAfter(closing)) {
                                continue; // Skip if exceeds closing time
                            }

                            SlotDto slot = new SlotDto();
                            slot.setCourtId(court.getId());
                            slot.setCourtNumber(courtNumber);
                            slot.setDate(date);
                            slot.setStartTime(slotStart);
                            slot.setEndTime(slotEnd);
                            slot.setAvailable(true);
                            slot.setDurationHours(hours); // CRITICAL: Set duration

                            slots.add(slot);
                        }
                        slotStart = slotStart.plusHours(1); // Move to next hour
                    }
                }
                slotService.createSlots(slots);
            }
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

        if (court.getIsArchived() != null && court.getIsArchived()) {
            throw new IllegalStateException("Court already deleted");
        }

        List<Booking> activeBookings = bookingRepository.findActiveBookingsByCourtId(id);

        if (!activeBookings.isEmpty()) {
            for (Booking booking : activeBookings) {
                try {
                    refundBooking(booking);
                    Slot slot = booking.getSlot();
                    emailService.sendCourtDeletionNotification(
                            booking.getMember().getUser().getEmail(),
                            court.getName(),
                            slot.getDate(),
                            slot.getStartTime(),
                            booking.getTotalAmount()
                    );
                    updateBookingStatus(booking);

                    addCompensationPoints(booking.getMember());
                } catch (Exception e) {
                    log.error("Error processing booking {} during court deletion: {}", booking.getId(), e.getMessage());
                }
            }
        }

        // 軟刪除球場
        courtRepository.softDeleteCourt(id, LocalDateTime.now());
        log.info("Court {} has been soft deleted", id);
    }

    private void refundBooking(Booking booking) {
        log.info("Processing refund for booking ID: {}, Amount: ${}",
                booking.getId(), booking.getTotalAmount());

        Payment payment = booking.getPayment();
        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);

        Slot slot = booking.getSlot();
        slot.setAvailable(true);
        slotRepository.save(slot);


        BookingSlot bookingSlot = booking.getBookingSlot();
        bookingSlot.setStatus("CANCELLED");
        bookingSlotRepository.save(bookingSlot);
    }

    private void updateBookingStatus(Booking booking) {
        booking.setStatus("CANCELLED_DUE_TO_COURT_DELETION");
        bookingRepository.save(booking);
    }

    private void addCompensationPoints(Member member) {
        int currentPoints = member.getPointBalance();
        member.setPointBalance(currentPoints + 200); // 添加200積分作為補償
        memberRepository.save(member);
        log.info("Added 200 compensation points to member ID: {}", member.getId());
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