package com.funkard.gradelens.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisMeta {
    private double sharpness;
    private double glare;
    private double skew;
}
