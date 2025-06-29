package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.*;
import com.pickleball_backend.pickleball.entity.*;
import com.pickleball_backend.pickleball.exception.*;
import com.pickleball_backend.pickleball.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MembershipTierRepository tierRepository;
    private final UserAccountRepository userAccountRepository;
    private final VoucherRepository voucherRepository;
    private final WalletRepository walletRepository;
    private final EmailService emailService;
    private final TierService tierService;

    public MemberDashboardDto getMemberDashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        User user = account.getUser();
        Member member = memberRepository.findByUserId(user.getId());

        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        if (member.getTier() == null) {
            // Try to auto-assign tier first
            tierService.recalculateMemberTier(member);
            member = memberRepository.findByUserId(user.getId()); // Refresh
        }

        if (member.getTier() == null) {
            // Use default tier if still null
            MembershipTier defaultTier = tierRepository.findByTierName(MembershipTier.TierName.SILVER);
            member.setTier(defaultTier);
            memberRepository.save(member);
        }

        List<Voucher> vouchers = voucherRepository.findByTierId(member.getTier().getId());

        return new MemberDashboardDto(
                member.getTier().getTierName().name(),
                member.getPointBalance(),
                member.getTier().getBenefits(),
                vouchers.stream()
                        .filter(v -> v.getExpiryDate() != null && v.getExpiryDate().isAfter(LocalDate.now()))
                        .map(v -> new VoucherDto(
                                v.getId(),
                                v.getCode(),
                                v.getDiscountAmount(),
                                v.getRequestPoints(),
                                v.getExpiryDate()
                        ))
                        .collect(Collectors.toList()),
                // Add these new fields
                member.getTier().getMinPoints(),
                member.getTier().getMaxPoints()
        );
    }

    public Integer getMemberIdByUsername(String username) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));
        Member member = memberRepository.findByUserId(account.getUser().getId());
        return member.getId();
    }

    @Transactional
    public VoucherRedemptionResponse redeemVoucher(Integer voucherId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        User user = account.getUser();
        Member member = memberRepository.findByUserId(user.getId());

        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        if (member.getTier() == null) {
            throw new ValidationException("No tier assigned to member");
        }

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        // Validate voucher
        if (voucher.getTier().getId() != member.getTier().getId()) {
            throw new ValidationException("Voucher not available for your tier");
        }
        if (member.getPointBalance() < voucher.getRequestPoints()) {
            throw new ValidationException("Insufficient points");
        }
        if (voucher.getExpiryDate() == null || voucher.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Voucher has expired");
        }

        // Deduct points
        member.setPointBalance(member.getPointBalance() - voucher.getRequestPoints());
        memberRepository.save(member);

        // Recalculate tier
        tierService.recalculateMemberTier(member);

        // Generate unique voucher code
        String uniqueCode = voucher.getCode() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create redeemed voucher
        Voucher redeemedVoucher = new Voucher();
        redeemedVoucher.setCode(uniqueCode);
        redeemedVoucher.setDiscountAmount(voucher.getDiscountAmount());
        redeemedVoucher.setRequestPoints(voucher.getRequestPoints());
        redeemedVoucher.setExpiryDate(voucher.getExpiryDate());
        redeemedVoucher.setTier(voucher.getTier());
        redeemedVoucher.setMember(member);
        voucherRepository.save(redeemedVoucher);

        // Send confirmation email
        emailService.sendVoucherEmail(
                user.getEmail(),
                "Voucher Redemption Confirmation",
                "You redeemed voucher: " + uniqueCode + "\nNew balance: " + member.getPointBalance() + " points"
        );

        return new VoucherRedemptionResponse(
                uniqueCode,
                voucher.getExpiryDate(),
                member.getPointBalance()
        );
    }

    @Transactional
    public void addPoints(int points) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        Member member = memberRepository.findByUserId(account.getUser().getId());
        member.setPointBalance(member.getPointBalance() + points);
        memberRepository.save(member);

        tierService.recalculateMemberTier(member);  // Upgrade tier if needed
    }

    public List<TierDto> getAllAvailableTiers() {
        return tierService.getAllTiers().stream()
                .map(tier -> {
                    TierDto dto = new TierDto();
                    dto.setId(tier.getId());
                    // Fix tier name case
                    dto.setTierName(MembershipTier.TierName.valueOf(tier.getTierName().name()));
                    dto.setBenefits(tier.getBenefits());
                    dto.setMinPoints(tier.getMinPoints());
                    dto.setMaxPoints(tier.getMaxPoints());
                    dto.setActive(tier.isActive());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<VoucherDto> getRedeemHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        User user = account.getUser();
        Member member = memberRepository.findByUserId(user.getId());

        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        // Get all vouchers associated with the member
        List<Voucher> redeemedVouchers = voucherRepository.findByMemberIdIsNotNull();

        return redeemedVouchers.stream()
                .filter(v -> v.getMember().getId().equals(member.getId())) // Filter by current member
                .map(v -> new VoucherDto(
                        v.getId(),
                        v.getCode(),
                        v.getDiscountAmount(),
                        v.getRequestPoints(),
                        v.getExpiryDate()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void initializeWallet(Integer memberId) {
        Wallet wallet = new Wallet();
        wallet.setBalance(100.00); // Initial balance
        wallet.setMember(memberRepository.findById(memberId).orElseThrow());
        walletRepository.save(wallet);
    }
}