package com.funkard.dto;

import com.funkard.model.PendingValue;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 📝 Request DTO per submit valore personalizzato "Altro"
 */
@Data
public class SubmitPendingValueRequest {
    
    @NotNull(message = "Il tipo è obbligatorio")
    private PendingValue.ValueType type;
    
    @NotBlank(message = "Il valore non può essere vuoto")
    private String value;
}

