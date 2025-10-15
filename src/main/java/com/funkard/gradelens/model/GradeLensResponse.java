package com.funkard.gradelens.model;

import java.util.List;

public class GradeLensResponse {
    public double overall;
    public Subgrades subgrades;
    public List<DiagnosticType> diagnostics;
    public AnalysisMeta analysisMeta;
}
