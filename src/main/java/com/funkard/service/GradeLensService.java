package com.funkard.service;

import com.funkard.model.GradeLensResult;
import com.funkard.repository.GradeLensRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class GradeLensService {

    private final R2Service r2Service;
    private final GradeLensRepository gradeLensRepository;

    public GradeLensService(R2Service r2Service, GradeLensRepository gradeLensRepository) {
        this.r2Service = r2Service;
        this.gradeLensRepository = gradeLensRepository;
    }

    public GradeLensResult analyzeCard(
            String userCardId,
            MultipartFile frontImage,
            MultipartFile backImage,
            List<MultipartFile> extraImages
    ) throws Exception {

        // Upload immagini principali
        String frontUrl = r2Service.uploadUserCardFile(frontImage, userCardId, "gradelens-front");
        String backUrl = r2Service.uploadUserCardFile(backImage, userCardId, "gradelens-back");

        if (extraImages != null) {
            int idx = 0;
            for (MultipartFile extra : extraImages) {
                if (extra != null && !extra.isEmpty()) {
                    r2Service.uploadUserCardFile(extra, userCardId, "gradelens-extra-" + (idx++));
                }
            }
        }

        // Mock AI scoring
        double corners = mockScore();
        double edges = mockScore();
        double surface = mockScore();
        double centering = mockScore();
        double overall = Math.round(((corners + edges + surface + centering) / 4) * 10.0) / 10.0;

        GradeLensResult result = new GradeLensResult();
        result.setUserCardId(userCardId);
        result.setCorners(corners);
        result.setEdges(edges);
        result.setSurface(surface);
        result.setCentering(centering);
        result.setOverallGrade(overall);
        result.setAiModel("Funkard-GradeLens-v1");
        result.setSource("AI");

        return gradeLensRepository.save(result);
    }

    public List<GradeLensResult> getResultsByUserCard(String userCardId) {
        return gradeLensRepository.findByUserCardId(userCardId);
    }

    private double mockScore() {
        return Math.round(((Math.random() * 2) + 8) * 10.0) / 10.0; // 8.0 - 10.0 step 0.1
    }
}
