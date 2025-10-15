package com.funkard.service;

import com.funkard.model.dto.GradeRequest;
import java.util.HashMap;
import java.util.Map;

public class GradeCalculator {
    public static Map<String, Double> computeSubgrades(GradeRequest req) {
        Map<String, Double> sub = new HashMap<>();
        // Esempio: calcolo semplificato, replica la logica TS
        sub.put("centering", Math.max(0, 10 - req.getOffCenterPct() * 0.1));
        sub.put("corners", Math.max(0, 10 - req.getCornersCount() * req.getCornersSeverity() * 0.5));
        sub.put("edges", Math.max(0, 10 - req.getEdgesCount() * req.getEdgesSeverity() * 0.5));
        sub.put("surface", Math.max(0, 10 - req.getSurfaceCount() * req.getSurfaceSeverity() * 0.5));
        if (req.isCreasesPresent()) {
            sub.put("creases", Math.max(0, 10 - ((double) req.getCreasesSeverity()) * 2));
        } else {
            sub.put("creases", 10.0);
        }
        return sub;
    }

    public static double computeOverall(Map<String, Double> sub) {
        // Media pesata, esempio
        double sum = 0;
        int count = 0;
        for (double v : sub.values()) {
            sum += v;
            count++;
        }
        return count > 0 ? Math.round((sum / count) * 100.0) / 100.0 : 0;
    }

    public static String labelFromGrade(double grade) {
        if (grade >= 9.5) return "GEM-MT";
        if (grade >= 9) return "MINT";
        if (grade >= 8) return "NM-MT";
        if (grade >= 7) return "NM";
        if (grade >= 6) return "EX-MT";
        if (grade >= 5) return "EX";
        if (grade >= 4) return "VG-EX";
        if (grade >= 3) return "VG";
        if (grade >= 2) return "G";
        return "POOR";
    }
}
