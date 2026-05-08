package com.shufuroom.features.booking.controller;

import com.shufuroom.features.booking.dto.CreateBookingRequest;
import com.shufuroom.features.booking.model.Booking;
import com.shufuroom.features.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository; // <-- 1. Inject the database

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
}