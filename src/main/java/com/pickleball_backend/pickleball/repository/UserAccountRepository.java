package com.pickleball_backend.pickleball.repository;

import com.pickleball_backend.pickleball.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository // Add this annotation
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByUser_Email(String email);
    Optional<UserAccount> findByResetPasswordToken(String token);
    Optional<UserAccount> findByUser_Id(Integer userId);
}