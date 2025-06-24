package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.*;
import com.pickleball_backend.pickleball.entity.*;
import com.pickleball_backend.pickleball.exception.ResourceNotFoundException;
import com.pickleball_backend.pickleball.exception.ValidationException;
import com.pickleball_backend.pickleball.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final CourtRepository courtRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final UserAccountRepository userAccountRepository;
    private final EmailService emailService;
    private final CancellationRequestRepository cancellationRequestRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private static final String CANCELLED_STATUS = "CANCELLED";

    @Transactional
    public BookingResponseDto bookCourt(BookingRequestDto request) {
        // 1. Get authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        // 2. Get member
        Member member = memberRepository.findByUserId(account.getUser().getId());
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        // 3. Get slot
        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        // 4. Validate slot duration matches request
        if (slot.getDurationHours() != request.getDurationHours()) {
            throw new ValidationException("Selected slot duration does not match request");
        }

        // 5. Check slot availability
        if (!slot.isAvailable() || isSlotBooked(slot.getId())) {
            throw new IllegalStateException("Slot is not available");
        }

        // 6. Get court and calculate price
        Court court = courtRepository.findById(slot.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        double amount = calculateBookingAmount(court, slot, request.getDurationHours());

        // 7. Create payment
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDate.now());
        payment = paymentRepository.save(payment);

        // 8. Create booking
        Booking booking = new Booking();
        booking.setBookingDate(LocalDate.now());
        booking.setTotalAmount(amount);
        String bookingStatus = "CONFIRMED";
        if (bookingStatus.length() > 50) {
            bookingStatus = bookingStatus.substring(0, 50);
        }
        booking.setStatus(bookingStatus);
        booking.setMember(member);
        booking.setSlot(slot);
        booking.setPayment(payment);
        booking.setPurpose(request.getPurpose());
        booking.setNumberOfPlayers(request.getNumberOfPlayers());
        booking = bookingRepository.save(booking);  // Save to generate ID

        // 9. Create BookingSlot record
        BookingSlot bookingSlot = new BookingSlot();
        bookingSlot.setBooking(booking);
        bookingSlot.setSlot(slot);
        String statusValue = "BOOKED";
        if (statusValue.length() > 50) {
            statusValue = statusValue.substring(0, 50);
        }
        bookingSlot.setStatus(statusValue);

        bookingSlotRepository.save(bookingSlot);

        // 10. Update slot availability
        slot.setAvailable(false);
        slotRepository.save(slot);

        // 11. Generate receipt
        emailService.sendBookingConfirmation(account.getUser().getEmail(), booking, court, slot);

        // 12. Create response with duration
        BookingResponseDto response = mapToBookingResponse(booking, court, slot);
        response.setDurationHours(request.getDurationHours());
        return response;
    }

    private boolean isSlotBooked(Integer slotId) {
        if (slotId == null) return false;
        return bookingSlotRepository.existsBySlotIdAndStatus(slotId, "BOOKED");
    }

    private double calculateBookingAmount(Court court, Slot slot, int durationHours) {
        LocalTime startTime = slot.getStartTime();
        LocalTime endTime = slot.getEndTime();

        // Handle null pricing safely
        double peakHourlyPrice = court.getPeakHourlyPrice() != null ?
                court.getPeakHourlyPrice() : 0.0;
        double offPeakHourlyPrice = court.getOffPeakHourlyPrice() != null ?
                court.getOffPeakHourlyPrice() : 0.0;

        // Default to off-peak rate
        double hourlyRate = offPeakHourlyPrice;

        // Only check peak times if defined
        if (court.getPeakStartTime() != null && court.getPeakEndTime() != null) {
            try {
                LocalTime peakStart = LocalTime.parse(court.getPeakStartTime());
                LocalTime peakEnd = LocalTime.parse(court.getPeakEndTime());

                // Check if slot falls entirely within peak hours
                if (!startTime.isBefore(peakStart) && !endTime.isAfter(peakEnd)) {
                    hourlyRate = peakHourlyPrice;
                }
            } catch (DateTimeParseException e) {
                log.error("Invalid peak time format: {}", e.getMessage());
                // Maintain off-peak rate if parsing fails
            }
        }

        // CORRECTED: Use validated durationHours parameter
        return hourlyRate * durationHours;
    }

    private BookingResponseDto mapToBookingResponse(Booking booking, Court court, Slot slot) {
        BookingResponseDto response = new BookingResponseDto();
        response.setBookingId(booking.getId());
        response.setCourtName(court.getName());
        response.setCourtLocation(court.getLocation());
        response.setSlotDate(slot.getDate());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setTotalAmount(booking.getTotalAmount());
        response.setBookingStatus(booking.getStatus());
        response.setPurpose(booking.getPurpose());
        response.setNumberOfPlayers(booking.getNumberOfPlayers());
        response.setCourtNumber(slot.getCourtNumber());
        return response;
    }



    @Transactional
    public CancellationResponse cancelBooking(Integer bookingId, String username) {
        // 1. Get user account
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        // 2. Get booking with relations
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // 3. Verify booking ownership
        if (!booking.getMember().getUser().getId().equals(account.getUser().getId())) {
            throw new ValidationException("You can only cancel your own bookings");
        }

        // 4. Get slot from booking
        Slot slot = booking.getSlot();

        // 5. Check cancellation eligibility
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getStartTime());
        if (LocalDateTime.now().plusHours(1).isAfter(slotDateTime)) {
            throw new ValidationException("Cannot cancel within 1 hour of booking");
        }

        // 6. Update booking status (DO NOT free slot yet - wait for admin approval)
        String newStatus = "CANCELLATION_REQUESTED";
        if (newStatus.length() > 50) {
            newStatus = newStatus.substring(0, 50);
        }
        booking.setStatus(newStatus);
        bookingRepository.save(booking);

        // 7. Create cancellation request
        CancellationRequest request = new CancellationRequest();
        request.setBooking(booking);
        request.setRequestDate(LocalDate.now());
        String requestStatus = "PENDING";
        if (requestStatus.length() > 50) {
            requestStatus = requestStatus.substring(0, 50);
        }
        request.setStatus(requestStatus);

        request.setReason("User requested cancellation");
        request.setApprovedBy(null);
        cancellationRequestRepository.save(request);


        // 8. Get court for email
        Court courtObj = courtRepository.findById(slot.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        emailService.sendCancellationConfirmation(
                booking.getMember().getUser().getEmail(),
                booking,
                slot,
                courtObj
        );

        // 10. Return response
        return new CancellationResponse(
                request.getId(),
                bookingId,
                "PENDING",
                LocalDate.now(),
                "Cancellation request submitted. Waiting for admin approval."
        );
    }

    public List<SlotResponseDto> getAvailableSlots(LocalDate date) {
        return slotRepository.findByDateAndIsAvailableTrue(date).stream()
                .map(slot -> {
                    SlotResponseDto dto = new SlotResponseDto();
                    dto.setId(slot.getId());
                    dto.setCourtId(slot.getCourtId());
                    dto.setDate(slot.getDate());
                    dto.setStartTime(slot.getStartTime());
                    dto.setEndTime(slot.getEndTime());
                    dto.setStatus("AVAILABLE");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PendingCancellationRequestDto> getPendingCancellationRequests() {
        return cancellationRequestRepository.findByStatus("PENDING").stream()
                .map(request -> {
                    // Safe navigation through relationships
                    Booking booking = request.getBooking();
                    if (booking == null) return null;

                    Slot slot = booking.getSlot();
                    if (slot == null) return null;

                    // Get court safely
                    Court court = courtRepository.findById(slot.getCourtId()).orElse(null);

                    // Get member name safely
                    String memberName = Optional.ofNullable(booking.getMember())
                            .map(Member::getUser)
                            .map(User::getName)
                            .orElse("Unknown Member");

                    return new PendingCancellationRequestDto(
                            request.getId(),
                            booking.getId(),
                            memberName,
                            slot.getDate(),
                            slot.getStartTime(),
                            court != null ? court.getName() : "Court not found",
                            request.getReason()
                    );
                })
                .filter(Objects::nonNull)  // Requires java.util.Objects import
                .collect(Collectors.toList());
    }

    @Transactional
    public CancellationResponse processCancellation(Integer requestId, boolean approve) {
        CancellationRequest request = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new ValidationException("Request already processed");
        }

        Booking booking = request.getBooking();
        BookingSlot bookingSlot = booking.getBookingSlot();
        Slot slot = bookingSlot.getSlot();
        Court court = courtRepository.findById(slot.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        if (approve) {
            // 1. Free up the slot
            slot.setAvailable(true);
            slotRepository.save(slot);

            // 2. Update booking status
            String bookingStatus = "CANCELLED";
            if (bookingStatus.length() > 50) {
                bookingStatus = bookingStatus.substring(0, 50);
            }
            booking.setStatus(bookingStatus);
            bookingRepository.save(booking);

            // 3. Update booking slot status
            String slotStatus = "CANCELLED";
            if (slotStatus.length() > 50) {
                slotStatus = slotStatus.substring(0, 50);
            }
            bookingSlot.setStatus(slotStatus);
            bookingSlotRepository.save(bookingSlot);

            // 4. Update request
            request.setStatus("APPROVED");

            // Get current admin ID
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User adminUser = userRepository.findByUserAccount_Username(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
            request.setApprovedBy(adminUser.getId());
        } else {
            // Reject request - revert changes
            String bookingStatus = "CONFIRMED";
            if (bookingStatus.length() > 50) {
                bookingStatus = bookingStatus.substring(0, 50);
            }
            booking.setStatus(bookingStatus);
            bookingRepository.save(booking);

            // Keep booking slot as booked
            String slotStatus = "BOOKED";
            if (slotStatus.length() > 50) {
                slotStatus = slotStatus.substring(0, 50);
            }
            bookingSlot.setStatus(slotStatus);

            request.setStatus("REJECTED");
        }

        cancellationRequestRepository.save(request);

        // Send decision email
        emailService.sendCancellationDecision(
                booking.getMember().getUser().getEmail(),
                booking,
                slot,
                court.getName(),
                approve
        );

        return new CancellationResponse(
                request.getId(),
                booking.getId(),
                request.getStatus(),
                request.getRequestDate(),
                approve ? "Cancellation approved" : "Cancellation rejected"
        );
    }

    public List<BookingHistoryDto> getBookingHistory(Integer memberId, String status) {
        List<Booking> bookings = bookingRepository.findByMemberId(memberId);

        return bookings.stream()
                .filter(booking -> status == null || booking.getStatus().equalsIgnoreCase(status))
                .map(booking -> {
                    Slot slot = booking.getSlot();
                    Court court = courtRepository.findById(slot.getCourtId())
                            .orElse(new Court());

                    BookingHistoryDto dto = new BookingHistoryDto();
                    dto.setId(booking.getId());
                    dto.setCourtName(court.getName());
                    dto.setLocation(court.getLocation());
                    dto.setDate(slot.getDate());
                    dto.setStartTime(slot.getStartTime());
                    dto.setEndTime(slot.getEndTime());
                    dto.setAmount(booking.getTotalAmount());
                    dto.setStatus(booking.getStatus());
                    dto.setCreatedAt(booking.getBookingDate());
                    dto.setPurpose(booking.getPurpose());
                    dto.setPlayers(booking.getNumberOfPlayers());
                    dto.setCourtNumber(slot.getCourtNumber());

                    // Add duration to history
                    dto.setDurationHours(slot.getDurationHours());

                    return dto;
                })
                .collect(Collectors.toList());
    }
}