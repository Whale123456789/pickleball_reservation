package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.slot.id = :slotId AND b.status <> :status")
    boolean existsBySlotIdAndStatusNot(@Param("slotId") Integer slotId, @Param("status") String status);

    boolean existsBySlotIdAndStatusNotIn(Integer slotId, List<String> statuses);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.bookingSlot WHERE b.id = :id")
    Optional<Booking> findByIdWithRelations(@Param("id") Integer id);

    List<Booking> findByMemberId(Integer memberId);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.slot s " +
            "WHERE s.courtId = :courtId " +
            "AND b.status NOT IN ('CANCELLED', 'COMPLETED', 'CANCELLED_DUE_TO_COURT_DELETION')")
    List<Booking> findActiveBookingsByCourtId(@Param("courtId") Integer courtId);
}