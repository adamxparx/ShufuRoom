package com.shufuroom.features.booking.service;

import com.shufuroom.features.booking.model.Booking;
import com.shufuroom.features.booking.repository.BookingRepository;
import com.shufuroom.features.room.model.Room;
import com.shufuroom.features.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomService roomService; 

    public Booking createBooking(Booking booking, String guestId) {
        Room room = roomService.getRoomById(booking.getRoomId()); // Verify room exists
        booking.setGuestId(guestId);
        
        // FIXED: Using a standard String instead of the Enum
        booking.setStatus("PENDING"); 
        
        return bookingRepository.save(booking);
    }

    public List<Booking> getMyBookings(String guestId) {
        return bookingRepository.findByGuestId(guestId);
    }

    // FIXED: Removed the broken getMyRequests method entirely! 
    // We handle this directly in the BookingController using the Room ID now.

    // FIXED: Changed 'Booking.BookingStatus newStatus' to 'String newStatus'
    public Booking updateBookingStatus(Long bookingId, String newStatus, String hostId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Room room = roomService.getRoomById(booking.getRoomId());
        if (!room.getHostId().equals(hostId)) {
            throw new RuntimeException("Unauthorized: You do not own this room.");
        }

        // FIXED: Using the standard String
        booking.setStatus(newStatus.toUpperCase());
        return bookingRepository.save(booking);
    }
}