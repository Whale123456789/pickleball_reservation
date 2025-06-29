package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByMemberId(Integer memberId); // Must return Optional
}