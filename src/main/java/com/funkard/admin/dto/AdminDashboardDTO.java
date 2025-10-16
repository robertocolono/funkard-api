package com.funkard.admin.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    
    private NotificationStats notifications;
    private MarketStats market;
    private GradingStats grading;
    private UserStats users;
    private SupportStats support;
    private List<MarketTrendPoint> marketTrend;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationStats {
        private int active;
        private int resolved;
        private int critical;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketStats {
        private int totalProducts;
        private double avgValueChange;
        private int newThisWeek;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradingStats {
        private int total;
        private int errors;
        private int inProgress;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private int total;
        private int flagged;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportStats {
        private int open;
        private int resolved;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketTrendPoint {
        private String date;
        private double value;
    }
}
