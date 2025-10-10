package com.funkard.gradelens;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class HeuristicAiProvider {
    private final Random rnd = new Random();

    public GradeResult analyze(String imageUrl) {
        GradeResult r = new GradeResult();

        // Range realistici 6.0–10.0
        double centering = 6.5 + rnd.nextDouble() * 3.5;
        double edges     = 6.0 + rnd.nextDouble() * 4.0;
        double corners   = 6.0 + rnd.nextDouble() * 4.0;
        double surface   = 6.0 + rnd.nextDouble() * 4.0;

        r.subscores = Map.of(
            "centering", round(centering),
            "edges", round(edges),
            "corners", round(corners),
            "surface", round(surface)
        );

        // Pesi PSA/BGS-like: 35/25/25/15
        r.grade = round(
            centering * 0.35 +
            edges * 0.25 +
            corners * 0.25 +
            surface * 0.15
        );

        GradeResult.ValueEstimate v = new GradeResult.ValueEstimate();
        // crescita non lineare, in EUR
        v.mid  = round(100 * Math.pow(r.grade / 10.0, 3));
        v.low  = round(v.mid * 0.8);
        v.high = round(v.mid * 1.2);
        r.valueEstimate = v;

        r.notes = "Analisi gratuita (simulazione realistica basata su criteri PSA/BGS).";
        return r;
    }

    private double round(double x) {
        return Math.round(x * 10.0) / 10.0;
    }
}