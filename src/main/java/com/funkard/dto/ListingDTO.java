package com.funkard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ListingDTO {
    private String id;
    private String title;
    private String description;
    private Double price;
    private String status;
    private LocalDateTime createdAt;
    private String sellerId;
    private String cardId;
}