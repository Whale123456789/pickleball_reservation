package com.pickleball_backend.pickleball.controller;

import com.pickleball_backend.pickleball.dto.ProfileDto;
import com.pickleball_backend.pickleball.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferencesController {

    private final ProfileService profileService;

    /**
     * Retrieves user preferences
     *
     * @param authentication Spring Security authentication object
     * @return ProfileDto containing user preferences
     */
    @GetMapping
    public ResponseEntity<ProfileDto> getPreferences(Authentication authentication) {
        // Extract username from authentication context
        String username = authentication.getName();

        // Fetch preferences via service layer
        ProfileDto preferences = profileService.getProfile(username);

        return ResponseEntity.ok(preferences);
    }

    /**
     * Updates user preferences
     *
     * @param authentication Spring Security authentication object
     * @param preferencesDto DTO containing updated preferences
     * @return Updated ProfileDto
     */
    @PutMapping
    public ResponseEntity<ProfileDto> updatePreferences(
            Authentication authentication,
            @RequestBody ProfileDto preferencesDto
    ) {
        // Extract username from authentication context
        String username = authentication.getName();

        // Update preferences via service layer
        ProfileDto updatedPreferences = profileService.updatePreferences(username, preferencesDto);

        return ResponseEntity.ok(updatedPreferences);
    }
}