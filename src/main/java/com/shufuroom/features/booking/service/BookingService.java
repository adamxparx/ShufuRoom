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

    // Importing across feature boundaries is normal and expected here
    @Autowired
    private RoomService roomService; 

    public Booking createBooking(Booking booking, String guestId) {
        Room room = roomService.getRoomById(booking.getRoomId()); // Verify room exists
        booking.setGuestId(guestId);
        booking.setStatus(Booking.BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }

    public List<Booking> getMyBookings(String guestId) {
        return bookingRepository.findByGuestId(guestId);
    }

    public List<Booking> getMyRequests(String hostId) {
        return bookingRepository.findRequestsByHostId(hostId);
    }

    public Booking updateBookingStatus(Long bookingId, Booking.BookingStatus newStatus, String hostId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Room room = roomService.getRoomById(booking.getRoomId());
        if (!room.getHostId().equals(hostId)) {
            throw new RuntimeException("Unauthorized: You do not own this room.");
        }

        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }
}