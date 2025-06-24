package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.AdminRegistrationDTO;
import com.pickleball_backend.pickleball.entity.*;
import com.pickleball_backend.pickleball.repository.*;
import com.pickleball_backend.pickleball.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException; // Changed import
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public Admin login(String username, String password) {
        Optional<UserAccount> userAccountOpt = userAccountRepository.findByUsername(username);
        if (userAccountOpt.isEmpty()) return null;

        UserAccount account = userAccountOpt.get();
        if (!passwordEncoder.matches(password, account.getPassword())) {
            return null;
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            return null;
        }


        return adminRepository.findByUsername(username).orElse(null);
    }

    @Override
    @Transactional
    public Admin register(AdminRegistrationDTO registrationDTO) {
        if (userAccountRepository.findByUsername(registrationDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create and save user first
        User user = new User();
        user.setName(registrationDTO.getName());
        user.setGender(registrationDTO.getGender());
        user.setEmail(registrationDTO.getEmail());
        user.setPhone(registrationDTO.getPhone());
        user.setDob(registrationDTO.getDob());
        user.setUserType("ADMIN");
        User savedUser = userRepository.save(user);  // Save user first

        // Create and save user account
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(registrationDTO.getUsername());
        userAccount.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        userAccount.setStatus("ACTIVE");
        userAccount.setUser(savedUser);  // Use saved user with ID
        userAccountRepository.save(userAccount);

        // Create admin - let @MapsId handle ID assignment
        Admin admin = new Admin();
        admin.setUser(savedUser);  // Set the saved user
        admin.setPosition(registrationDTO.getPosition());

        return adminRepository.save(admin);
    }

    @Override
    public String loginAndGetToken(String username, String password) {
        Admin admin = login(username, password);
        if (admin == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return jwtService.generateToken(
                username,
                "ROLE_ADMIN" // Fixed authority
        );
    }
}