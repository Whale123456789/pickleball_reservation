package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.*;
import com.pickleball_backend.pickleball.entity.*;
import com.pickleball_backend.pickleball.repository.*;
import com.pickleball_backend.pickleball.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private final MembershipTierRepository membershipTierRepository; // Correct repository
    private final BookingRepository bookingRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    public void register(RegistrationDto dto) {
        if (userAccountRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setGender(dto.getGender());
        user.setDob(LocalDate.parse(dto.getDob()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setUserType(dto.getUserType());

        userRepository.save(user);

        UserAccount account = new UserAccount();
        account.setUsername(dto.getUsername());
        account.setPassword(passwordEncoder.encode(dto.getPassword()));
        account.setUser(user);
        userAccountRepository.save(account);

        if ("ADMIN".equalsIgnoreCase(dto.getUserType())) {
            Admin admin = new Admin();
            admin.setUser(user);
            admin.setPosition(dto.getPosition());
            adminRepository.save(admin);
        }

        // Create membership tier assignment
        assignDefaultMembershipTier(user);

        log.info("User registered successfully with Silver tier: {}", dto.getUsername());
    }

    private void assignDefaultMembershipTier(User user) {
        // Use the correct repository: membershipTierRepository
        MembershipTier silverTier = membershipTierRepository.findByTierName(MembershipTier.TierName.SILVER);

        // Create default tier if not exists
        if (silverTier == null) {
            silverTier = new MembershipTier();
            silverTier.setTierName(MembershipTier.TierName.SILVER);
            silverTier.setMinPoints(0);
            silverTier.setMaxPoints(2000);
            silverTier.setBenefits("10% discount");
            silverTier = membershipTierRepository.save(silverTier);
        }

        // Create member record
        Member member = new Member();
        member.setUser(user);
        member.setTier(silverTier);
        member.setPointBalance(1000);
        memberRepository.save(member);
        user.setMember(member);
        userRepository.save(user);
        log.info("Assigned default Silver tier to user: {}", user.getEmail());
    }

    @Override
    public Optional<String> login(LoginDto dto) {
        log.debug("Login attempt for: {}", dto.getUsernameOrEmail());

        Optional<UserAccount> accountOpt = userAccountRepository.findByUsername(dto.getUsernameOrEmail());

        if (accountOpt.isEmpty()) {
            accountOpt = userAccountRepository.findByUser_Email(dto.getUsernameOrEmail());
        }

        Optional<String> token = accountOpt
                .filter(account -> passwordEncoder.matches(dto.getPassword(), account.getPassword()))
                .map(account -> jwtService.generateToken(
                        account.getUsername(),
                        account.getUser().getUserType().toUpperCase() // Send uppercase role
                ));

        if (token.isPresent()) {
            log.info("Login successful for: {}", dto.getUsernameOrEmail());
        } else {
            log.warn("Login failed for: {}", dto.getUsernameOrEmail());
        }

        return token;
    }

    @Override
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        String errorMsg = "User not found with email: " + email;
                        log.error(errorMsg);
                        return new RuntimeException(errorMsg);
                    });

            UserAccount account = userAccountRepository.findByUser_Email(email)
                    .orElseThrow(() -> {
                        String errorMsg = "User account not found for email: " + email;
                        log.error(errorMsg);
                        return new RuntimeException(errorMsg);
                    });

            // Generate token
            String token = UUID.randomUUID().toString();
            account.setResetPasswordToken(token);
            account.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
            userAccountRepository.save(account);

            log.info("Generated reset token: {} for email: {}", token, email);

            // Send email
            String resetLink = "http://localhost:5173/reset-password/" + token;
            emailService.sendPasswordResetEmail(email, resetLink);
            log.info("Password reset email sent to: {}", email);

        } catch (Exception e) {
            log.error("Password reset failed for: {}", email, e);
            throw e;
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset attempt with token: {}", token);

        try {
            UserAccount account = userAccountRepository.findByResetPasswordToken(token)
                    .orElseThrow(() -> {
                        String errorMsg = "Invalid password reset token: " + token;
                        log.error(errorMsg);
                        return new RuntimeException(errorMsg);
                    });

            if (account.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
                String errorMsg = "Token expired: " + token;
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            account.setPassword(passwordEncoder.encode(newPassword));
            account.setResetPasswordToken(null);
            account.setResetPasswordTokenExpiry(null);
            userAccountRepository.save(account);

            log.info("Password reset successful for user: {}", account.getUsername());

        } catch (Exception e) {
            log.error("Password reset failed for token: {}", token, e);
            throw e;
        }
    }



}