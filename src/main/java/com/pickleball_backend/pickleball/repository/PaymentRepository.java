package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}