/*package com.pickleball_backend.pickleball.config;

import com.pickleball_backend.pickleball.entity.MembershipTier;
import com.pickleball_backend.pickleball.repository.MembershipTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MembershipTierRepository membershipTierRepository;

    @Override
    public void run(String... args) {
        // Create default tiers if they don't exist
        createTierIfNotExists("Bronze");
        createTierIfNotExists("Silver");
        createTierIfNotExists("Gold");
    }

    private void createTierIfNotExists(String tierName) {
        if (membershipTierRepository.findByTierName(tierName).isEmpty()) {
            MembershipTier tier = new MembershipTier();
            tier.setTierName(tierName);  // Ensure this matches the field name
            tier.setBenefits("Default benefits");
            membershipTierRepository.save(tier);
            log.info("Created tier: {}", tierName);
        } else {
            log.info("Tier already exists: {}", tierName);
        }
    }
}*/