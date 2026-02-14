package com.shufuroom.controller;

import com.shufuroom.dto.LoginRequest;
import com.shufuroom.dto.LoginResponse;
import com.shufuroom.dto.RegisterRequest;
import com.shufuroom.model.UserProfile;
import com.shufuroom.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed: Registration could not be completed" + e.getMessage()));
        }
    }
}
