package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.*;
import com.pickleball_backend.pickleball.entity.Court;
import com.pickleball_backend.pickleball.entity.Member;
import com.pickleball_backend.pickleball.entity.UserAccount;
import com.pickleball_backend.pickleball.entity.Wallet;
import com.pickleball_backend.pickleball.exception.ResourceNotFoundException;
import com.pickleball_backend.pickleball.exception.ValidationException;
import com.pickleball_backend.pickleball.repository.MemberRepository;
import com.pickleball_backend.pickleball.repository.UserAccountRepository;
import com.pickleball_backend.pickleball.repository.WalletRepository;
import com.pickleball_backend.pickleball.service.BookingService;
import com.pickleball_backend.pickleball.service.CourtService;
import com.pickleball_backend.pickleball.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final BookingService bookingService;
    private final CourtService courtService;
    private final WalletRepository walletRepository;
    private final UserAccountRepository userAccountRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MemberDashboardDto> getDashboard() {
        return ResponseEntity.ok(memberService.getMemberDashboard());
    }

    @GetMapping("/tiers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TierDto>> getAvailableTiers() {
        return ResponseEntity.ok(memberService.getAllAvailableTiers());
    }

    @PostMapping("/vouchers/redeem/{voucherId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<VoucherRedemptionResponse> redeemVoucher(
            @PathVariable Integer voucherId) {
        return ResponseEntity.ok(memberService.redeemVoucher(voucherId));
    }

    @PostMapping("/add-points")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> addPoints(@RequestParam int points) {
        memberService.addPoints(points);
        return ResponseEntity.ok(points + " points added");
    }

    @GetMapping("/courts/availability")
    @PreAuthorize("hasRole('USER')") // Add security annotation
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Handle null date
        if (date == null) {
            date = LocalDate.now();
        }

        // Validate date not in past
        if (date.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("Date must be today or in future");
        }

        List<SlotResponseDto> slots = bookingService.getAvailableSlots(date);

        return slots.isEmpty()
                ? ResponseEntity.ok("No available slots found for " + date)
                : ResponseEntity.ok(slots);
    }

    @GetMapping("/redeem-history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<VoucherDto>> getRedeemHistory() {
        return ResponseEntity.ok(memberService.getRedeemHistory());
    }

    @GetMapping("/courts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAllCourtsForMember() {
        try {
            List<Court> courts = courtService.getAllCourts();
            return new ResponseEntity<>(courts, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving courts");
        }
    }

    @GetMapping("/courts/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCourtById(@PathVariable Integer id) {
        try {
            Court court = courtService.getCourtByIdForMember(id);
            if (court == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Court not found");
            }
            return new ResponseEntity<>(court, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving court");
        }
    }

    @PostMapping("/wallet/init")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> initializeWallet() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Member member = memberRepository.findByUserId(account.getUser().getId());

        walletRepository.findByMemberId(member.getId()).ifPresentOrElse(
                wallet -> { throw new ValidationException("Wallet already exists"); },
                () -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setMember(member);
                    newWallet.setBalance(0.00);
                    walletRepository.save(newWallet);
                }
        );

        return ResponseEntity.ok("Wallet initialized successfully");
    }
}