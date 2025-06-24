package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.ProfileDto;
import com.pickleball_backend.pickleball.entity.User;
import com.pickleball_backend.pickleball.entity.UserAccount;
import com.pickleball_backend.pickleball.repository.UserAccountRepository;
import com.pickleball_backend.pickleball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProfileService {

    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ProfileDto getProfile(String username) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        User user = account.getUser();

        return ProfileDto.builder()
                .username(account.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dob(user.getDob())
                .gender(user.getGender())
                .userType(user.getUserType())
                .requestedUserType(user.getRequestedUserType())
                .bookingsMade(user.getBookingsMade())
                .bookingHours(user.getBookingHours())
                .sumosJoined(user.getSumosJoined())
                .amountSpent(user.getAmountSpent())
                .status(account.getStatus())
                .theme(account.getTheme())
                .emailNotifications(account.isEmailNotifications())
                .pushNotifications(account.isPushNotifications())
                .profileImage(account.getProfileImage())
                .build();
    }

    public ProfileDto updateProfile(String username, ProfileDto profileDto) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        User user = account.getUser();

        // Update basic profile information
        user.setName(profileDto.getName());
        user.setGender(profileDto.getGender());
        user.setPhone(profileDto.getPhone());
        user.setDob(profileDto.getDob());

        // Handle user type change request
        boolean typeChangeRequested = false;
        if (profileDto.getRequestedUserType() != null &&
                !profileDto.getRequestedUserType().equals(user.getUserType()) &&
                ("User".equals(profileDto.getRequestedUserType()) ||
                        "Coach".equals(profileDto.getRequestedUserType()) ||
                        "EventOrganizer".equals(profileDto.getRequestedUserType()))) {

            user.setRequestedUserType(profileDto.getRequestedUserType());
            typeChangeRequested = true;
            log.info("User type change requested: {} -> {}",
                    user.getUserType(), profileDto.getRequestedUserType());
        }

        // Update username if changed
        if (profileDto.getUsername() != null &&
                !profileDto.getUsername().equals(username)) {
            if (userAccountRepository.findByUsername(profileDto.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            account.setUsername(profileDto.getUsername());
        }

        // Set status to PENDING if type change was requested
        if (typeChangeRequested) {
            account.setStatus("PENDING");
            log.info("Account status set to PENDING for user: {}", username);
        }

        if (profileDto.getTheme() != null) {
            account.setTheme(profileDto.getTheme());
        }
        account.setEmailNotifications(profileDto.isEmailNotifications());
        account.setPushNotifications(profileDto.isPushNotifications());

        // Save changes
        userRepository.save(user);
        userAccountRepository.save(account);

        // Return updated profile DTO
        return ProfileDto.builder()
                .username(account.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dob(user.getDob())
                .gender(user.getGender())
                .userType(user.getUserType())
                .requestedUserType(user.getRequestedUserType())
                .bookingsMade(user.getBookingsMade())
                .bookingHours(user.getBookingHours())
                .sumosJoined(user.getSumosJoined())
                .amountSpent(user.getAmountSpent())
                .status(account.getStatus())
                .theme(account.getTheme())
                .emailNotifications(account.isEmailNotifications())
                .pushNotifications(account.isPushNotifications())
                .profileImage(account.getProfileImage())
                .build();
    }
    public void updateProfilePhoto(String username, String filename) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        account.setProfileImage(filename);
        userAccountRepository.save(account);
    }

    public void removeProfilePhoto(String username) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Get current filename before removal
        String currentImage = account.getProfileImage();

        // Remove reference from database
        account.setProfileImage(null);
        userAccountRepository.save(account);

        // Delete physical file
        if (currentImage != null && !currentImage.isEmpty()) {
            try {
                fileStorageService.delete(currentImage);
            } catch (Exception e) {
                log.error("Failed to delete profile image file: {}", currentImage, e);
            }
        }
    }

    @Transactional
    public ProfileDto updatePreferences(String username, ProfileDto preferencesDto) {
        // Retrieve user account
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.info("Updating preferences for user: {}", username);

        // Update theme preference
        if (preferencesDto.getTheme() != null) {
            account.setTheme(preferencesDto.getTheme());
            log.debug("Updated theme to: {}", preferencesDto.getTheme());
        }

        // Update notification preferences
        account.setEmailNotifications(preferencesDto.isEmailNotifications());
        account.setPushNotifications(preferencesDto.isPushNotifications());

        log.debug("Email notifications: {}", preferencesDto.isEmailNotifications());
        log.debug("Push notifications: {}", preferencesDto.isPushNotifications());

        // Save updated preferences
        userAccountRepository.save(account);

        // Return updated preferences
        return ProfileDto.builder()
                .theme(account.getTheme())
                .emailNotifications(account.isEmailNotifications())
                .pushNotifications(account.isPushNotifications())
                .build();
    }
}