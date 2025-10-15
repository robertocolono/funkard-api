package com.funkard.model.dto;

import lombok.Data;

@Data
public class GradeRequest {
    private String cardId;
    private String service;
    private double offCenterPct;
    private int cornersCount;
    private int cornersSeverity;
    private int edgesCount;
    private int edgesSeverity;
    private int surfaceCount;
    private int surfaceSeverity;
    private boolean creasesPresent;
    private int creasesSeverity;
}
