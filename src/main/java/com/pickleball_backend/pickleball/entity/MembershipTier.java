package com.pickleball_backend.pickleball.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "membershiptier")
@Getter
@Setter
@NoArgsConstructor
public class MembershipTier {
    public enum TierName { SILVER, GOLD, PLATINUM }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private TierName tierName;

    private String benefits;

    private int minPoints;
    private int maxPoints;

    // Add this new field
    private boolean active = true;

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Voucher> vouchers;
}