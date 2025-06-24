package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserAccount_Username(String username);
    List<User> findByRequestedUserTypeIsNotNull(); // New method for pending requests
}