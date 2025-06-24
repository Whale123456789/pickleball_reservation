package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    @Query("SELECT a FROM Admin a " +
            "JOIN a.user u " +
            "JOIN u.userAccount ua " +  // Now we can join through User
            "WHERE ua.username = :username")
    Optional<Admin> findByUsername(@Param("username") String username);

    @Query("SELECT a FROM Admin a WHERE a.user.id = :userId")
    Optional<Admin> findByUserId(@Param("userId") Integer userId);
}
