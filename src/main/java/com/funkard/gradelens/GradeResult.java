package com.funkard.gradelens;

import java.util.Map;

public class GradeResult {
    public double grade;
    public Map<String, Double> subscores;
    public ValueEstimate valueEstimate;
    public String notes;
    public String mode = "heuristic"; // per chiarezza

    public static class ValueEstimate {
        public double low;
        public double mid;
        public double high;
        public String currency = "EUR";
    }
}