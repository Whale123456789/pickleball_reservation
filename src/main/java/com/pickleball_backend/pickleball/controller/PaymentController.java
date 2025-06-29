package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.BookingResponseDto;
import com.pickleball_backend.pickleball.dto.PaymentRequestDto;
import com.pickleball_backend.pickleball.exception.InsufficientBalanceException;
import com.pickleball_backend.pickleball.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final BookingService bookingService;

    @PostMapping("/wallet")
    public ResponseEntity<?> processWalletPayment(@RequestBody PaymentRequestDto request) {
        try {
            BookingResponseDto response = bookingService.bookCourt(request.getBookingRequest());
            return ResponseEntity.ok(response);
        } catch (InsufficientBalanceException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "INSUFFICIENT_BALANCE", "message", e.getMessage())
            );
        }
    }
}