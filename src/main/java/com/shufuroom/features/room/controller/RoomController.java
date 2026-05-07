package com.shufuroom.features.room.controller;

import com.shufuroom.features.room.model.Room;
import com.shufuroom.features.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // PROTECTED: Only authenticated users can create a room
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room, @AuthenticationPrincipal Jwt jwt) {
        // Securely grab the user's Supabase UUID from the auth token
        String hostId = jwt.getSubject(); 
        Room createdRoom = roomService.createRoom(room, hostId);
        return ResponseEntity.ok(createdRoom);
    }

    // PROTECTED: Fetch only the rooms hosted by the logged-in user
    @GetMapping("/my-listings")
    public ResponseEntity<List<Room>> getMyListings(@AuthenticationPrincipal Jwt jwt) {
        String hostId = jwt.getSubject();
        return ResponseEntity.ok(roomService.getRoomsByHost(hostId));
    }

    // PUBLIC: Anyone can view the list of all rooms (for the Home Page)
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    // PUBLIC: Anyone can view a specific room's details
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }
}