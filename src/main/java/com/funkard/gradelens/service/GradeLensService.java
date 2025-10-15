package com.funkard.gradelens.service;

import com.funkard.gradelens.model.*;
import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeLensService {

    static {
        // Carica libreria nativa OpenCV dal dependency jar
        OpenCV.loadLocally();
    }

    public boolean testOpenCV() {
        Mat img = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("OpenCV OK, matrix: " + img.dump());
        return true;
    }

    public GradeLensResponse analyze(String frontImageUrl, String backImageUrl) {
        // Stub iniziale: genera valori plausibili e calcola overall
        Subgrades sub = new Subgrades(9.12, 8.60, 8.95, 8.73);
        double overall = 0.4 * sub.centering + 0.25 * sub.corners + 0.2 * sub.edges + 0.15 * sub.surface;
        overall = Math.round(overall * 100.0) / 100.0;

        var resp = new GradeLensResponse();
        resp.subgrades = sub;
        resp.overall = overall;
        resp.diagnostics = List.of(DiagnosticType.CORNER_DAMAGE_MINOR, DiagnosticType.SLIGHT_GLARE_DETECTED);
        resp.analysisMeta = new AnalysisMeta(0.92, 0.10, 0.03);
        return resp;
    }
}
