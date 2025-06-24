package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Integer> {
    List<Slot> findByCourtIdAndDateBetween(Integer courtId, LocalDate startDate, LocalDate endDate);
    List<Slot> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Slot> findByCourtIdAndDateAndIsAvailableTrue(Integer courtId, LocalDate date);
    List<Slot> findByDateAndIsAvailableTrue(LocalDate date);
    List<Slot> findByCourtIdAndDateAndStatus(Integer courtId, LocalDate date, String status);
    List<Slot> findByCourtIdAndStatus(Integer courtId, String status);
    List<Slot> findByCourtIdAndDateBetweenAndStatus(Integer courtId, LocalDate startDate, LocalDate endDate, String status);
    List<Slot> findByCourtIdAndDateBetweenAndIsAvailableTrue(
            Integer courtId,
            LocalDate startDate,
            LocalDate endDate
    );
}