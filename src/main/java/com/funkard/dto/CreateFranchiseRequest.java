package com.funkard.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 📝 Request DTO per creazione franchise manuale (admin)
 */
@Data
public class CreateFranchiseRequest {
    
    @NotBlank(message = "La categoria è obbligatoria")
    private String category;
    
    @NotBlank(message = "Il nome è obbligatorio")
    private String name;
}

