package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MembershipTierRepository extends JpaRepository<MembershipTier, Integer> {
    MembershipTier findByTierName(MembershipTier.TierName tierName);

    // Added custom query
    @Query("SELECT t FROM MembershipTier t ORDER BY t.minPoints ASC")
    List<MembershipTier> findAllOrderedByMinPoints();

    @Query("SELECT t FROM MembershipTier t WHERE t.active = true ORDER BY t.minPoints ASC")
    List<MembershipTier> findAllByActiveTrueOrderByMinPointsAsc();
}