package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.BookingSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingSlotRepository extends JpaRepository<BookingSlot, Integer> {
    boolean existsBySlotIdAndStatus(Integer slotId, String status);
}