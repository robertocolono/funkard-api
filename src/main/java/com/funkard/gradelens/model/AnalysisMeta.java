package com.funkard.gradelens.model;

public class AnalysisMeta {
    public double sharpness;
    public double glare;
    public double skew;

    public AnalysisMeta() {}

    public AnalysisMeta(double sharpness, double glare, double skew) {
        this.sharpness = sharpness;
        this.glare = glare;
        this.skew = skew;
    }
}
