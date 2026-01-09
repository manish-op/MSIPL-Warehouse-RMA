package com.serverManagement.server.management.dto.rma.dashboard;

import java.util.List;

public class RmaDashboardStatsDto {

    private long totalRequests;
    private long totalItems;
    private long repairedCount;
    private long unrepairedCount;
    private List<DailyTrendDto> dailyTrends;
    private List<String> recentRmaNumbers; // Recent RMA numbers for display

    // SLA Compliance Stats
    private Long totalWithTat; // Requests that have TAT defined
    private Long onTrackCount; // Green - within TAT with buffer
    private Long atRiskCount; // Yellow - nearing deadline
    private Long breachedCount; // Red - past due date
    private Double complianceRate; // % of closed requests within TAT

    public RmaDashboardStatsDto() {
    }

    public RmaDashboardStatsDto(long totalRequests, long totalItems, long repairedCount, long unrepairedCount,
            List<DailyTrendDto> dailyTrends) {
        this.totalRequests = totalRequests;
        this.totalItems = totalItems;
        this.repairedCount = repairedCount;
        this.unrepairedCount = unrepairedCount;
        this.dailyTrends = dailyTrends;
    }

    public RmaDashboardStatsDto(long totalRequests, long totalItems, long repairedCount, long unrepairedCount,
            List<DailyTrendDto> dailyTrends, List<String> recentRmaNumbers) {
        this.totalRequests = totalRequests;
        this.totalItems = totalItems;
        this.repairedCount = repairedCount;
        this.unrepairedCount = unrepairedCount;
        this.dailyTrends = dailyTrends;
        this.recentRmaNumbers = recentRmaNumbers;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getRepairedCount() {
        return repairedCount;
    }

    public void setRepairedCount(long repairedCount) {
        this.repairedCount = repairedCount;
    }

    public long getUnrepairedCount() {
        return unrepairedCount;
    }

    public void setUnrepairedCount(long unrepairedCount) {
        this.unrepairedCount = unrepairedCount;
    }

    public List<DailyTrendDto> getDailyTrends() {
        return dailyTrends;
    }

    public void setDailyTrends(List<DailyTrendDto> dailyTrends) {
        this.dailyTrends = dailyTrends;
    }

    public List<String> getRecentRmaNumbers() {
        return recentRmaNumbers;
    }

    public void setRecentRmaNumbers(List<String> recentRmaNumbers) {
        this.recentRmaNumbers = recentRmaNumbers;
    }

    // SLA Compliance Getters and Setters
    public Long getTotalWithTat() {
        return totalWithTat;
    }

    public void setTotalWithTat(Long totalWithTat) {
        this.totalWithTat = totalWithTat;
    }

    public Long getOnTrackCount() {
        return onTrackCount;
    }

    public void setOnTrackCount(Long onTrackCount) {
        this.onTrackCount = onTrackCount;
    }

    public Long getAtRiskCount() {
        return atRiskCount;
    }

    public void setAtRiskCount(Long atRiskCount) {
        this.atRiskCount = atRiskCount;
    }

    public Long getBreachedCount() {
        return breachedCount;
    }

    public void setBreachedCount(Long breachedCount) {
        this.breachedCount = breachedCount;
    }

    public Double getComplianceRate() {
        return complianceRate;
    }

    public void setComplianceRate(Double complianceRate) {
        this.complianceRate = complianceRate;
    }
}
