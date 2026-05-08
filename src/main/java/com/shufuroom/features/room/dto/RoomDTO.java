package com.shufuroom.features.room.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoomDTO {
    private Long id;
    private String name;
    private String description;
    private Integer beds;
    private Double price;
    private String imageUrl;
    private String hostName; // <-- This is what React wants!
    private LocalDateTime createdAt;
}