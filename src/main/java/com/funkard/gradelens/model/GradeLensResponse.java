package com.funkard.gradelens.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class GradeLensResponse {
    private double overall;
    private Subgrades subgrades;
    private List<String> diagnostics;
    private AnalysisMeta analysisMeta;
    private LocalDateTime gradedAt;
}
