package com.funkard.admin.dto;

import java.time.LocalDate;

public class SupportStatsDTO {
    public LocalDate day;
    public long opened;
    public long closed;

    public SupportStatsDTO() {}

    public SupportStatsDTO(LocalDate day, long opened, long closed) {
        this.day = day;
        this.opened = opened;
        this.closed = closed;
    }

    // Getters and Setters
    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }

    public long getOpened() { return opened; }
    public void setOpened(long opened) { this.opened = opened; }

    public long getClosed() { return closed; }
    public void setClosed(long closed) { this.closed = closed; }
}
