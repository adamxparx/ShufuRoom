package com.shufuroom.features.room.service;

import com.shufuroom.features.room.model.Room;
import com.shufuroom.features.room.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public Room createRoom(Room room, String hostId) {
        room.setHostId(hostId);
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public List<Room> getRoomsByHost(String hostId) {
        return roomRepository.findByHostId(hostId);
    }
}