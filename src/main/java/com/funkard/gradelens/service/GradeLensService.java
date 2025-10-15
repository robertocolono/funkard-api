package com.funkard.gradelens.service;

import com.funkard.gradelens.model.*;
import com.funkard.model.CardSource;
import com.funkard.model.UserCard;
import com.funkard.repository.UserCardRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        double overall = 0.4 * sub.getCentering() + 0.25 * sub.getCorners() + 0.2 * sub.getEdges() + 0.15 * sub.getSurface();
        overall = Math.round(overall * 100.0) / 100.0;

        List<String> diagnostics = List.of(
                "CORNER_DAMAGE_MINOR",
                "SLIGHT_GLARE_DETECTED"
        );

        AnalysisMeta meta = new AnalysisMeta(0.92, 0.10, 0.03);
        return new GradeLensResponse(overall, sub, diagnostics, meta, LocalDateTime.now());
    }

    private final UserCardRepository userCardRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GradeLensService(UserCardRepository userCardRepository) {
        this.userCardRepository = userCardRepository;
    }

    public String saveGradedCard(
            String userId,
            String name,
            String setName,
            String condition,
            String frontImageUrl,
            String backImageUrl,
            Map<String, Object> subgrades,
            Double overallGrade,
            Map<String, Object> analysisMeta,
            List<String> diagnostics
    ) {
        UserCard card = new UserCard();
        card.setUserId(userId);
        card.setName(name);
        card.setSetName(setName);
        card.setCondition(condition);
        card.setFrontImage(frontImageUrl);
        card.setBackImage(backImageUrl);
        card.setGradeService("FUNKARD_GRADELENS");
        card.setGradeOverall(overallGrade);
        card.setGradeLabel(null); // opzionale: etichetta derivata da overall
        card.setGradedAt(LocalDateTime.now());
        card.setPermanent(true);
        card.setSource(CardSource.GRADELENS);

        // Serializza i subgrades in JSON (colonna JSONB)
        try {
            card.setSubgrades(objectMapper.writeValueAsString(subgrades));
        } catch (JsonProcessingException e) {
            // fallback: toString
            card.setSubgrades(subgrades != null ? subgrades.toString() : null);
        }

        // Stima di valore (placeholder logico)
        if (overallGrade != null) {
            card.setEstimatedValue(overallGrade * 10);
        }

        // NB: analysisMeta e diagnostics non hanno ancora colonne dedicate nella tabella user_cards
        // Se necessario, si può aggiungere un campo JSONB separato in una migrazione futura.

        userCardRepository.save(card);
        return card.getId();
    }
}
