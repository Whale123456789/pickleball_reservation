package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    boolean existsByCode(String code);
    List<Voucher> findByTierId(Integer tierId);
    List<Voucher> findByMemberIdIsNotNull();
}