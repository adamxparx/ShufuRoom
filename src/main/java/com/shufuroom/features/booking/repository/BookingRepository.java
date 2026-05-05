package com.shufuroom.features.booking.repository;

import com.shufuroom.features.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByGuestId(String guestId);

    // Note the cross-domain query here: we assume 'Room' is mapped correctly by JPA
    @Query("SELECT b FROM Booking b JOIN Room r ON b.roomId = r.id WHERE r.hostId = :hostId")
    List<Booking> findRequestsByHostId(@Param("hostId") String hostId);
}