package com.shufuroom.features.booking.controller;

import com.shufuroom.features.booking.dto.CreateBookingRequest;
import com.shufuroom.features.booking.model.Booking;
import com.shufuroom.features.booking.repository.BookingRepository;
import com.shufuroom.features.profile.model.UserProfile;
import com.shufuroom.features.profile.repository.UserProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @PostMapping
    public ResponseEntity<?> createBooking(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateBookingRequest request) {

        // Get the logged-in user's ID
        String guestId = jwt.getSubject(); // Supabase UUID

        // 2. Build the database entity
        Booking newBooking = new Booking();
        newBooking.setRoomId(request.getRoomId());
        newBooking.setGuestId(guestId);
        newBooking.setCheckInDate(request.getCheckInDate());
        newBooking.setCheckOutDate(request.getCheckOutDate());
        newBooking.setStatus("PENDING");

        // 3. SAVE IT TO SUPABASE!
        bookingRepository.save(newBooking);

        System.out.println("✅ Saved booking to database for Room: " + request.getRoomId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking confirmed and saved successfully!"
        ));
    }

    @GetMapping("/my-trips")
    public ResponseEntity<List<Booking>> getMyTrips(@AuthenticationPrincipal Jwt jwt) {
        
        // 1. Get the securely authenticated user's ID
        String guestId = jwt.getSubject();
        
        // 2. Ask the database for all matching trips
        List<Booking> myBookings = bookingRepository.findByGuestId(guestId);
        
        System.out.println("✅ Found " + myBookings.size() + " trips for user: " + guestId);
        
        // 3. Send the list back to React
        return ResponseEntity.ok(myBookings);
    }

    // 1. Get all requests for a specific room
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Map<String, Object>>> getRoomBookings(@PathVariable Long roomId) {
        
        // 1. Fetch the raw bookings
        List<Booking> bookings = bookingRepository.findByRoomIdOrderByCheckInDateDesc(roomId);
        
        // 2. Loop through them and safely attach the guest's real name
        List<Map<String, Object>> formattedRequests = bookings.stream().map(booking -> {
            Map<String, Object> map = new HashMap<>();
            map.put("booking", booking);
            
            try {
                // Safely convert the String guestId back to a UUID for the lookup
                UUID userUuid = UUID.fromString(booking.getGuestId());
                UserProfile user = userProfileRepository.findById(userUuid).orElse(null);
                
                if (user != null) {
                    map.put("guestName", user.getFirstName() + " " + user.getLastName());
                } else {
                    map.put("guestName", "Unknown Guest");
                }
            } catch (Exception e) {
                // Fallback just in case the Supabase ID is formatted weirdly
                map.put("guestName", "Unknown Guest");
            }
            
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(formattedRequests);
    }

    // 2. Approve or Reject a booking
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long bookingId, @RequestParam String status) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // This sets it using a standard String and forces it to uppercase (e.g., "APPROVED")
        booking.setStatus(status.toUpperCase()); 
        
        bookingRepository.save(booking);
        return ResponseEntity.ok().body("{\"message\": \"Status updated successfully\"}");
    }
}