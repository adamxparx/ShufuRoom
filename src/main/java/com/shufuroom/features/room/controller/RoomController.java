package com.shufuroom.features.room.controller;

import com.shufuroom.features.profile.repository.UserProfileRepository;
import com.shufuroom.features.room.dto.RoomDTO;
import com.shufuroom.features.room.model.Room;
import com.shufuroom.features.room.repository.RoomRepository;
import com.shufuroom.features.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserProfileRepository userRepository;



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
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        
        // 1. Get the raw room from the database
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Room not found"));


        // 2. Lookup the user's name using the room's hostId
        // (Assuming you have a UserRepository or UserProfileRepository)
        String fetchedHostName = userRepository.findById(UUID.fromString(room.getHostId()))
            .map(user -> user.getFirstName() + " " + user.getLastName())
            .orElse("Unknown Host");
        
        // 3. Map it to the DTO
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setDescription(room.getDescription());
        dto.setBeds(room.getBeds());
        dto.setPrice(room.getPrice());
        dto.setImageUrl(room.getImageUrl());
        dto.setCreatedAt(room.getCreatedAt());
        
        dto.setHostName(fetchedHostName); // <-- Attach the name!

        // 4. Send the DTO to React
        return ResponseEntity.ok(dto);
}
}