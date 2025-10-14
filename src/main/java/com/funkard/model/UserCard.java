package com.funkard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_cards")
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String setName;

    @Column(nullable = false)
    private String condition;

    private Double estimatedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type = CardType.RAW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardSource source = CardSource.MANUAL;

    // Immagini principali
    private String frontImage;
    private String backImage;

    // Immagini RAW extra (angoli)
    private String cornerTopLeft;
    private String cornerTopRight;
    private String cornerBottomLeft;
    private String cornerBottomRight;

    // Alias / nuovi campi richiesti per upload RAW (compatibilità frontend)
    private String topLeftImage;
    private String topRightImage;
    private String bottomLeftImage;
    private String bottomRightImage;

    // Immagini RAW extra (bordi)
    private String edgeLeft;
    private String edgeRight;
    private String edgeTop;
    private String edgeBottom;

    private String edgeTopImage;    // alias
    private String edgeBottomImage; // alias
    private String edgeLeftImage;   // alias
    private String edgeRightImage;  // alias

    // Analisi / GradeLens
    private Double gradeValue;
    private String gradeLabel;
    private Double gradeConfidence;
    private String gradeReportUrl;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSetName() { return setName; }
    public void setSetName(String setName) { this.setName = setName; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Double getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(Double estimatedValue) { this.estimatedValue = estimatedValue; }

    public CardType getType() { return type; }
    public void setType(CardType type) { this.type = type; }

    public CardSource getSource() { return source; }
    public void setSource(CardSource source) { this.source = source; }

    public String getFrontImage() { return frontImage; }
    public void setFrontImage(String frontImage) { this.frontImage = frontImage; }

    public String getBackImage() { return backImage; }
    public void setBackImage(String backImage) { this.backImage = backImage; }

    public String getCornerTopLeft() { return cornerTopLeft; }
    public void setCornerTopLeft(String cornerTopLeft) { this.cornerTopLeft = cornerTopLeft; }

    public String getCornerTopRight() { return cornerTopRight; }
    public void setCornerTopRight(String cornerTopRight) { this.cornerTopRight = cornerTopRight; }

    public String getCornerBottomLeft() { return cornerBottomLeft; }
    public void setCornerBottomLeft(String cornerBottomLeft) { this.cornerBottomLeft = cornerBottomLeft; }

    public String getCornerBottomRight() { return cornerBottomRight; }
    public void setCornerBottomRight(String cornerBottomRight) { this.cornerBottomRight = cornerBottomRight; }

    public String getEdgeLeft() { return edgeLeft; }
    public void setEdgeLeft(String edgeLeft) { this.edgeLeft = edgeLeft; }

    public String getEdgeRight() { return edgeRight; }
    public void setEdgeRight(String edgeRight) { this.edgeRight = edgeRight; }

    public String getEdgeTop() { return edgeTop; }
    public void setEdgeTop(String edgeTop) { this.edgeTop = edgeTop; }

    public String getEdgeBottom() { return edgeBottom; }
    public void setEdgeBottom(String edgeBottom) { this.edgeBottom = edgeBottom; }

    public String getTopLeftImage() { return topLeftImage; }
    public void setTopLeftImage(String topLeftImage) { this.topLeftImage = topLeftImage; this.cornerTopLeft = topLeftImage; }
    public String getTopRightImage() { return topRightImage; }
    public void setTopRightImage(String topRightImage) { this.topRightImage = topRightImage; this.cornerTopRight = topRightImage; }
    public String getBottomLeftImage() { return bottomLeftImage; }
    public void setBottomLeftImage(String bottomLeftImage) { this.bottomLeftImage = bottomLeftImage; this.cornerBottomLeft = bottomLeftImage; }
    public String getBottomRightImage() { return bottomRightImage; }
    public void setBottomRightImage(String bottomRightImage) { this.bottomRightImage = bottomRightImage; this.cornerBottomRight = bottomRightImage; }

    public String getEdgeTopImage() { return edgeTopImage; }
    public void setEdgeTopImage(String edgeTopImage) { this.edgeTopImage = edgeTopImage; this.edgeTop = edgeTopImage; }
    public String getEdgeBottomImage() { return edgeBottomImage; }
    public void setEdgeBottomImage(String edgeBottomImage) { this.edgeBottomImage = edgeBottomImage; this.edgeBottom = edgeBottomImage; }
    public String getEdgeLeftImage() { return edgeLeftImage; }
    public void setEdgeLeftImage(String edgeLeftImage) { this.edgeLeftImage = edgeLeftImage; this.edgeLeft = edgeLeftImage; }
    public String getEdgeRightImage() { return edgeRightImage; }
    public void setEdgeRightImage(String edgeRightImage) { this.edgeRightImage = edgeRightImage; this.edgeRight = edgeRightImage; }

    public Double getGradeValue() { return gradeValue; }
    public void setGradeValue(Double gradeValue) { this.gradeValue = gradeValue; }

    public String getGradeLabel() { return gradeLabel; }
    public void setGradeLabel(String gradeLabel) { this.gradeLabel = gradeLabel; }

    public Double getGradeConfidence() { return gradeConfidence; }
    public void setGradeConfidence(Double gradeConfidence) { this.gradeConfidence = gradeConfidence; }

    public String getGradeReportUrl() { return gradeReportUrl; }
    public void setGradeReportUrl(String gradeReportUrl) { this.gradeReportUrl = gradeReportUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
