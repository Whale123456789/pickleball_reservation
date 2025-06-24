package com.pickleball_backend.pickleball.dto;

import com.pickleball_backend.pickleball.entity.MembershipTier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TierDto {
    private int id;
    private MembershipTier.TierName tierName;
    private String benefits;
    private int minPoints;
    private int maxPoints;
    private boolean active;
}