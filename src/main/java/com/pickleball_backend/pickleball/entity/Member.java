package com.pickleball_backend.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Member {
    @Id
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "tier_id")
    private MembershipTier tier;


    @Column(name = "point_balance", nullable = false, columnDefinition = "int default 0")
    private int pointBalance = 0;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Voucher> vouchers = new ArrayList<>();
}