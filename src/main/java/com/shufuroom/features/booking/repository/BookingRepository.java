package com.shufuroom.features.booking.repository;

import com.shufuroom.features.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Make sure to import this!

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Spring Boot's "Magic Naming" will automatically write the SQL for this!
    List<Booking> findByGuestId(String guestId);
    
}