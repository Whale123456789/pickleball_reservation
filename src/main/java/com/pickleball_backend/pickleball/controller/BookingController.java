package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.BookingRequestDto;
import com.pickleball_backend.pickleball.dto.BookingResponseDto;
import com.pickleball_backend.pickleball.dto.CancellationResponse;
import com.pickleball_backend.pickleball.entity.Member;
import com.pickleball_backend.pickleball.entity.UserAccount;
import com.pickleball_backend.pickleball.exception.ResourceNotFoundException;
import com.pickleball_backend.pickleball.repository.MemberRepository;
import com.pickleball_backend.pickleball.repository.UserAccountRepository;
import com.pickleball_backend.pickleball.service.BookingService;
import com.pickleball_backend.pickleball.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import com.pickleball_backend.pickleball.dto.BookingHistoryDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final UserAccountRepository userAccountRepository;

    @PostMapping("/bookings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> bookCourt(@RequestBody BookingRequestDto request) {
        try {
            BookingResponseDto response = bookingService.bookCourt(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "BOOKING_FAILED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingHistoryDto>> getBookingHistory(
            @RequestParam(required = false) String status) {
        // Get current user's member ID
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer memberId = memberService.getMemberIdByUsername(username);

        return ResponseEntity.ok(bookingService.getBookingHistory(memberId, status));
    }

/*    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingHistoryDto> getBookingDetails(@PathVariable Integer id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer memberId = memberService.getMemberIdByUsername(username);

        return ResponseEntity.ok(bookingService.getBookingDetails(id, memberId));
    }*/

    @PostMapping("/bookings/{bookingId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CancellationResponse> cancelBooking(
            @PathVariable Integer bookingId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CancellationResponse response = bookingService.cancelBooking(bookingId, username);
        return ResponseEntity.ok(response);
    }
}