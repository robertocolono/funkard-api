package com.funkard.admin.dto;

import java.util.List;
import lombok.Data;

@Data
public class AdminDashboardDTO {

    private NotificationStats notifications;
    private MarketStats market;
    private GradingStats grading;
    private UserStats users;
    private SupportStats support;
    private List<MarketTrendPoint> marketTrend;

    @Data
    public static class NotificationStats {
        private long active;
        private long resolved;
        private long critical;
    }

    @Data
    public static class MarketStats {
        private long totalProducts;
        private double avgValueChange;
        private long newThisWeek;
    }

    @Data
    public static class GradingStats {
        private long total;
        private long errors;
        private long inProgress;
    }

    @Data
    public static class UserStats {
        private long total;
        private long flagged;
    }

    @Data
    public static class SupportStats {
        private long open;
        private long resolved;
    }

    @Data
    public static class MarketTrendPoint {
        private String date;
        private double value;

        public MarketTrendPoint(String date, double value) {
            this.date = date;
            this.value = value;
        }
    }
}
