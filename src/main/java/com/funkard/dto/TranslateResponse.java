package com.funkard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📝 DTO per risposta traduzione
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslateResponse {
    private String translated;
}

