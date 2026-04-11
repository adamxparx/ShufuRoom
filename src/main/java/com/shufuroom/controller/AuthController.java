package com.shufuroom.controller;

import com.shufuroom.dto.ChangePasswordRequest;
import com.shufuroom.dto.LoginRequest;
import com.shufuroom.dto.LoginResponse;
import com.shufuroom.dto.RegisterRequest;
import com.shufuroom.model.UserProfile;
import com.shufuroom.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserProfileRepository profileRepository;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String supabaseAuthUrl;

    @Value("${supabase.anon.key}")
    private String supabaseAnonKey;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);

        Map<String, String> body = Map.of(
                "email", request.getEmail(),
                "password", request.getPassword()
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            String tokenUrl = supabaseAuthUrl + "/token?grant_type=password";
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

            String token = (String) response.getBody().get("access_token");
            return ResponseEntity.ok(new LoginResponse("Sucess: Authentication Successful", token, response.getBody().get("user")));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Failed: Invalid email or password", null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);

        Map<String, Object> body = Map.of(
                "email", request.getEmail(),
                "password", request.getPassword(),
                "data", Map.of(
                        "firstName", request.getFirstName(),
                        "lastName", request.getLastName()
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            String signupUrl = supabaseAuthUrl + "/signup";
            ResponseEntity<Map> response = restTemplate.postForEntity(signupUrl, entity, Map.class);

            if (response.getBody() != null && response.getBody().get("user") != null) {
                Map<String, Object> userMap = (Map<String, Object>) response.getBody().get("user");
                UUID supabaseId = UUID.fromString(userMap.get("id").toString());

                UserProfile userProfile = new UserProfile(
                        supabaseId,
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail()
                );

                profileRepository.save(userProfile);

                return ResponseEntity.ok(Map.of("message", "Success: User registered successfully"));

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Registration failed"));
            }

        } catch (HttpClientErrorException e) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> errorBody = mapper.readValue(e.getResponseBodyAsString(), Map.class);
                String msg = errorBody.get("msg") != null ? errorBody.get("msg").toString() : "Registration failed";

                return ResponseEntity.status(e.getStatusCode())
                        .body(Map.of("message", msg));

            } catch (Exception parseEx) {
                return ResponseEntity.status(e.getStatusCode())
                        .body(Map.of("message", "Registration failed"));
            }
        } catch (Exception e) {
            // other unexpected errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal Jwt jwt, 
                                            @RequestBody ChangePasswordRequest request) {
        // Extract user email from the JWT claims
        String email = jwt.getClaimAsString("email");
        String userToken = jwt.getTokenValue();

        // 1. Verify Current Password by attempting a "Sign In" to Supabase
        boolean isValid = verifyWithSupabase(email, request.getCurrentPassword());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Incorrect current password"));
        }

        // 2. If valid, Update the User in Supabase (PATCH /auth/v1/user)
        try {
            updateSupabaseUserPassword(userToken, request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Success: Password updated"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Update failed"));
        }
    }

    /**
     * Helper to re-authenticate the user against Supabase.
     */
    private boolean verifyWithSupabase(String email, String password) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);

        Map<String, String> body = Map.of("email", email, "password", password);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            String tokenUrl = supabaseAuthUrl + "/token?grant_type=password";
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false; // Authentication failed
        }
    }


    /**
     * Helper to update password using Supabase's /user endpoint.
     * Uses the user's specific JWT for authorization.
     */
    private void updateSupabaseUserPassword(String userToken, String newPassword) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);
        // Important: Use the specific user's token, not the service key
        headers.set("Authorization", "Bearer " + userToken);

        Map<String, String> body = Map.of("password", newPassword);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        // Supabase PATCH /user updates the authenticated user's details
        String userUpdateUrl = supabaseAuthUrl + "/user";
        
        // Using exchange because postForEntity doesn't support PATCH directly
        restTemplate.exchange(userUpdateUrl, HttpMethod.PATCH, entity, Map.class);
    }

}
