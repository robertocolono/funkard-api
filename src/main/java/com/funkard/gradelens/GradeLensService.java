package com.funkard.gradelens;

import com.funkard.model.GradeReport;
import com.funkard.repository.GradeReportRepository;
import org.springframework.stereotype.Service;

@Service
public class GradeLensService {

    private final HeuristicAiProvider heuristic;
    private final GradeReportRepository reports;

    public GradeLensService(HeuristicAiProvider heuristic, GradeReportRepository reports) {
        this.heuristic = heuristic;
        this.reports = reports;
    }

    public GradeReport analyzeAndStore(String imageUrl, boolean adShown) {
        GradeResult r = heuristic.analyze(imageUrl);

        GradeReport gr = new GradeReport();
        gr.setImageUrl(imageUrl);
        gr.setGrade(r.grade);
        gr.setCentering(r.subscores.get("centering"));
        gr.setEdges(r.subscores.get("edges"));
        gr.setCorners(r.subscores.get("corners"));
        gr.setSurface(r.subscores.get("surface"));
        gr.setValueLow(r.valueEstimate.low);
        gr.setValueMid(r.valueEstimate.mid);
        gr.setValueHigh(r.valueEstimate.high);
        gr.setCurrency(r.valueEstimate.currency);
        gr.setMode(r.mode);
        gr.setNotes(r.notes);
        gr.setAdShown(adShown);

        return reports.save(gr);
    }
}