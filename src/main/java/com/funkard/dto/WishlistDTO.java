package com.funkard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WishlistDTO {
    private String id;
    private String userId;
    private String cardId;
    private LocalDateTime createdAt;
}