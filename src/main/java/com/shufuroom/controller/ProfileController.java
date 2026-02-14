package com.shufuroom.controller;

import com.shufuroom.dto.EditProfile;
import com.shufuroom.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserProfileRepository profileRepository;

    @GetMapping("/me")
    public Object getMyProfile(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found."));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                           @RequestBody EditProfile updateData) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return profileRepository.findById(userId).map(existingProfile -> {

            if (updateData.getFirstName() != null) {
                existingProfile.setFirstName(updateData.getFirstName());
            }
            if (updateData.getLastName() != null) {
                existingProfile.setLastName(updateData.getLastName());
            }

            profileRepository.save(existingProfile);

            return ResponseEntity.ok(Map.of("message", "Success: Profile updated successfully",
                    "updatedProfile", existingProfile));
        }).orElse(ResponseEntity.status(404).body("Error: Profile not found"));
    }

}
