package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.TierDto;
import com.pickleball_backend.pickleball.dto.VoucherDto;
import com.pickleball_backend.pickleball.entity.MembershipTier;
import com.pickleball_backend.pickleball.entity.Voucher;
import com.pickleball_backend.pickleball.entity.Member;
import com.pickleball_backend.pickleball.exception.ResourceNotFoundException;
import com.pickleball_backend.pickleball.exception.ValidationException;
import com.pickleball_backend.pickleball.repository.MemberRepository;
import com.pickleball_backend.pickleball.repository.MembershipTierRepository;
import com.pickleball_backend.pickleball.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TierServiceImpl implements TierService {

    private static final Logger logger = LoggerFactory.getLogger(TierServiceImpl.class);

    private final MembershipTierRepository tierRepository;
    private final VoucherRepository voucherRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService; // Added for email notifications

    // Existing methods with enhancements...

    @Override
    @Transactional
    public MembershipTier createTier(TierDto tierDto) {
        MembershipTier tier = new MembershipTier();
        tier.setTierName(tierDto.getTierName());
        tier.setBenefits(tierDto.getBenefits());
        tier.setMinPoints(tierDto.getMinPoints());
        tier.setMaxPoints(tierDto.getMaxPoints());
        return createOrUpdateTier(tier); // Reuse common logic
    }

    @Override
    @Transactional
    public MembershipTier createOrUpdateTier(MembershipTier tier) {
        // Handle default tier conflict
        if (tier.getId() == 0 && tier.getTierName() == MembershipTier.TierName.SILVER) {
            MembershipTier existingSilver = tierRepository.findByTierName(MembershipTier.TierName.SILVER);
            if (existingSilver != null) {
                existingSilver.setMinPoints(tier.getMinPoints());
                existingSilver.setMaxPoints(tier.getMaxPoints());
                existingSilver.setBenefits(tier.getBenefits());
                tier = existingSilver;
            }
        }

        validateTier(tier);
        MembershipTier savedTier = tierRepository.save(tier);
        recalculateAllMembersTiers();

        // Validate tier hierarchy after update
        List<MembershipTier> allTiers = tierRepository.findAllOrderedByMinPoints();
        validateTierHierarchy(allTiers); // Added hierarchy validation

        return savedTier;
    }

    private void validateTier(MembershipTier tier) {
        List<MembershipTier> allTiers = tierRepository.findAllOrderedByMinPoints();

        // Check for overlaps (excluding current tier if updating)
        for (MembershipTier existing : allTiers) {
            if (tier.getId() != 0 && tier.getId() == existing.getId()) {
                continue;
            }

            // Improved overlap detection logic
            if ((tier.getMinPoints() <= existing.getMaxPoints() &&
                    tier.getMinPoints() >= existing.getMinPoints()) ||
                    (tier.getMaxPoints() <= existing.getMaxPoints() &&
                            tier.getMaxPoints() >= existing.getMinPoints())) {

                logger.error("Tier overlap detected: {} ({}-{}) with {} ({}-{})",
                        tier.getTierName(), tier.getMinPoints(), tier.getMaxPoints(),
                        existing.getTierName(), existing.getMinPoints(), existing.getMaxPoints());

                throw new ValidationException("Tier point ranges cannot overlap");
            }
        }

        // Validate tier order hierarchy
        if (tier.getMinPoints() >= tier.getMaxPoints()) {
            throw new ValidationException("Min points must be less than max points");
        }

        // Relaxed benefits regex
        if (tier.getBenefits() != null &&
                !tier.getBenefits().matches("(?i).*(\\d+%\\s*(discount|off)|\\d+\\s*free\\s*(bookings?|sessions?)).*")) {

            throw new ValidationException("Benefits must include discount (e.g., 10% off) or free bookings (e.g., 2 free sessions)");
        }
    }

    // Added tier hierarchy validation
    private void validateTierHierarchy(List<MembershipTier> tiers) {
        // Should be ordered by minPoints ascending
        for (int i = 1; i < tiers.size(); i++) {
            MembershipTier current = tiers.get(i);
            MembershipTier previous = tiers.get(i-1);

            if (current.getMinPoints() <= previous.getMaxPoints()) {
                throw new ValidationException(
                        "Tier hierarchy violation: " + previous.getTierName() +
                                " (max: " + previous.getMaxPoints() + ") must be less than " +
                                current.getTierName() + " (min: " + current.getMinPoints() + ")"
                );
            }
        }
    }

    @Override
    @Transactional
    public MembershipTier addVoucherToTier(String tierName, VoucherDto voucherDto) {
        // Handle case-insensitive tier name
        MembershipTier.TierName tierNameEnum;
        try {
            tierNameEnum = MembershipTier.TierName.valueOf(tierName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid tier name: " + tierName);
        }

        MembershipTier tier = tierRepository.findByTierName(tierNameEnum);
        if (tier == null) {
            throw new ValidationException("Tier not found: " + tierName);
        }

        if (voucherRepository.existsByCode(voucherDto.getCode())) {
            throw new ValidationException("Voucher code already exists");
        }

        Voucher voucher = new Voucher();
        voucher.setCode(voucherDto.getCode());
        voucher.setDiscountAmount(voucherDto.getDiscountAmount());
        voucher.setRequestPoints(voucherDto.getRequestPoints());
        voucher.setExpiryDate(voucherDto.getExpiryDate());
        voucher.setTier(tier);

        voucherRepository.save(voucher);
        return tier;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipTier> getAllTiers() {
        return tierRepository.findAllByActiveTrueOrderByMinPointsAsc();
    }



    @Override
    @Transactional
    public void recalculateMemberTier(Member member) {
        List<MembershipTier> activeTiers = tierRepository.findAllByActiveTrueOrderByMinPointsAsc();
        int userPoints = member.getPointBalance();
        MembershipTier newTier = null;

        // Find appropriate tier based on points
        for (MembershipTier tier : activeTiers) {
            if (userPoints >= tier.getMinPoints() &&
                    (tier.getMaxPoints() == Integer.MAX_VALUE ||
                            userPoints <= tier.getMaxPoints())) {
                newTier = tier;
                break;
            }
        }

        // Handle points exceeding highest tier
        if (newTier == null && !activeTiers.isEmpty()) {
            newTier = activeTiers.get(activeTiers.size() - 1); // Highest tier
        }

        // Update tier if changed
        if (newTier != null && !newTier.equals(member.getTier())) {
            // Send upgrade notification
            sendTierUpgradeEmail(member, newTier);  // Added email notification

            member.setTier(newTier);
            memberRepository.save(member);
            logger.info("Updated user {} tier from {} to {}",
                    member.getUser().getEmail(),
                    member.getTier() != null ? member.getTier().getTierName().name() : "NONE",
                    newTier.getTierName().name()
            );
        }
    }


    // Added email notification method
    private void sendTierUpgradeEmail(Member member, MembershipTier newTier) {
        String subject = "Congratulations! You've been upgraded to " + newTier.getTierName();
        String content = "Dear " + member.getUser().getName() + ",\n\n"
                + "You've been upgraded to " + newTier.getTierName() + " tier!\n"
                + "New benefits: " + newTier.getBenefits() + "\n\n"
                + "Enjoy your new perks!";

        emailService.sendVoucherEmail(
                member.getUser().getEmail(),
                subject,
                content
        );
    }

    private void recalculateAllMembersTiers() {
        try {
            List<MembershipTier> tiers = tierRepository.findAllOrderedByMinPoints();
            logger.info("Recalculating tiers for {} membership tiers", tiers.size());

            for (MembershipTier tier : tiers) {
                try {
                    int updated = memberRepository.updateMembersTier(
                            tier.getMinPoints(),
                            tier.getMaxPoints(),
                            tier
                    );
                    logger.info("Updated {} members to {} tier", updated, tier.getTierName());
                } catch (Exception e) {
                    logger.error("Error updating members for tier {}: {}",
                            tier.getTierName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error during tier recalculation: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public Voucher updateVoucher(Integer voucherId, VoucherDto voucherDto) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", voucherId));

        // Check for duplicate code if changed
        if (!voucher.getCode().equals(voucherDto.getCode())) {
            if (voucherRepository.existsByCode(voucherDto.getCode())) {
                throw new ValidationException("Voucher code already exists");
            }
        }

        voucher.setCode(voucherDto.getCode());
        voucher.setDiscountAmount(voucherDto.getDiscountAmount());
        voucher.setRequestPoints(voucherDto.getRequestPoints());
        voucher.setExpiryDate(voucherDto.getExpiryDate());

        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(Integer voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", voucherId));

        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional
    public void toggleTierStatus(Integer tierId, boolean active) {
        MembershipTier tier = tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Tier", "id", tierId));

        tier.setActive(active);
        tierRepository.save(tier);

        if (!active) {
            // Reassign members to default tier
            MembershipTier defaultTier = tierRepository.findByTierName(MembershipTier.TierName.SILVER);
            if (defaultTier == null) {
                throw new ValidationException("Default Silver tier not found");
            }
            memberRepository.reassignMembersFromTier(tierId, defaultTier);
        }
    }

    private void validateBenefitProgression(List<MembershipTier> tiers) {
        for (int i = 1; i < tiers.size(); i++) {
            MembershipTier lower = tiers.get(i-1);
            MembershipTier higher = tiers.get(i);

            if (!higher.getBenefits().contains("more") &&
                    !higher.getBenefits().contains("additional")) {
                logger.warn("Higher tier {} might not offer improved benefits over {}",
                        higher.getTierName(), lower.getTierName());
            }
        }
    }

}