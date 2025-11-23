package com.funkard.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 📝 Request DTO per proposta franchise
 */
@Data
public class ProposeFranchiseRequest {
    
    @NotBlank(message = "La categoria è obbligatoria")
    private String category;
    
    @NotBlank(message = "Il franchise è obbligatorio")
    private String franchise;
}

