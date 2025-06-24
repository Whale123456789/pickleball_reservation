package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.ProfileDto;
import com.pickleball_backend.pickleball.service.ProfileService;
import com.pickleball_backend.pickleball.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Add this import
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final ProfileService profileService;
    private final FileStorageService fileStorageService; // Add this

    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(Authentication authentication) {
        String username = authentication.getName();
        ProfileDto profile = profileService.getProfile(username);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody ProfileDto profileDto
    ) {
        String username = authentication.getName();
        try {
            ProfileDto updatedProfile = profileService.updateProfile(username, profileDto);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Modify the photo upload endpoint to return filename
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePhoto(
            Authentication authentication,
            @RequestParam("profileImage") MultipartFile file) {
        try {
            String username = authentication.getName();
            String filename = fileStorageService.store(file);
            profileService.updateProfilePhoto(username, filename);

            // Return filename as string
            return ResponseEntity.ok().body(filename);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/photo")
    public ResponseEntity<?> removeProfilePhoto(Authentication authentication) {
        String username = authentication.getName();
        try {
            profileService.removeProfilePhoto(username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Add this logging statement to see the actual error
            logger.error("Failed to remove profile photo for user: {}", username, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to remove profile photo: " + e.getMessage());
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(
            Authentication authentication,
            @RequestBody ProfileDto preferencesDto) {
        try {
            String username = authentication.getName();
            ProfileDto updatedPreferences = profileService.updatePreferences(username, preferencesDto);
            return ResponseEntity.ok(updatedPreferences);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}