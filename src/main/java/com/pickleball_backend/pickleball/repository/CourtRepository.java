package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CourtRepository extends JpaRepository<Court, Integer> {
    boolean existsByNameAndLocation(String name, String location);
    List<Court> findByIsArchivedFalseOrIsArchivedIsNull();
    List<Court> findAll();
    @Query("SELECT c FROM Court c WHERE c.isArchived = false OR c.isArchived IS NULL")
    List<Court> findActiveCourts();
    // Add this new method
    Optional<Court> findById(Integer id);

    @Modifying
    @Query("UPDATE Court c SET c.isArchived = true, c.archiveTimestamp = :timestamp WHERE c.id = :id")
    void softDeleteCourt(@Param("id") Integer id, @Param("timestamp") LocalDateTime timestamp);
}