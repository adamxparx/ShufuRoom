package com.shufuroom.features.booking.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings") // This matches your Supabase table name!
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private String guestId; // Storing the Supabase Auth UUID as a String

    @Column(name = "start_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}