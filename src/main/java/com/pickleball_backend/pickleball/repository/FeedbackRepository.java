package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Feedback;
import com.pickleball_backend.pickleball.entity.Feedback.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            TargetType targetType,
            Integer targetId
    );

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.targetType = :targetType AND f.targetId = :targetId")
    Double findAverageRatingByTarget(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Integer targetId
    );

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.targetType = :targetType AND f.targetId = :targetId")
    Integer countByTarget(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Integer targetId
    );

    List<Feedback> findByUserId(Integer userId);
}