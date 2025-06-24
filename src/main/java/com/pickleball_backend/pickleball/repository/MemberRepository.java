package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Member;
import com.pickleball_backend.pickleball.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.tier = :tier WHERE m.pointBalance BETWEEN :min AND :max")
    int updateMembersTier(
            @Param("min") int min,
            @Param("max") int max,
            @Param("tier") MembershipTier tier
    );

    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.tier = :newTier WHERE m.tier.id = :oldTierId")
    int reassignMembersFromTier(
            @Param("oldTierId") Integer oldTierId,
            @Param("newTier") MembershipTier newTier
    );

    Member findByUserId(Integer userId);
}