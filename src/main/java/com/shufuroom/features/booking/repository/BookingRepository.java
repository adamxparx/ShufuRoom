package com.shufuroom.features.booking.repository;

import com.shufuroom.features.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByGuestId(String guestId);
    List<Booking> findByRoomIdOrderByCheckInDateDesc(Long roomId);
    
}