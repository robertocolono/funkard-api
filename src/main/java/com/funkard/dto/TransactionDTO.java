package com.funkard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private String id;
    private String buyerId;
    private String listingId;
    private Double price;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}