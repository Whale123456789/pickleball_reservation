package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.CancellationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CancellationRequestRepository extends JpaRepository<CancellationRequest, Integer> {
    List<CancellationRequest> findByStatus(String status);

    @Query("SELECT cr FROM CancellationRequest cr " +
            "JOIN FETCH cr.booking b " +
            "JOIN FETCH b.slot s " +
            "JOIN FETCH b.member m " +
            "JOIN FETCH m.user u " +
            "WHERE cr.status = :status")
    List<CancellationRequest> findByStatusWithRelations(@Param("status") String status);
}