package com.serverManagement.server.management.dto.rma;

public class RmaDashboardStatsDto {

    private long totalRequests;
    private long totalItems;
    private long repairedCount;
    private long unrepairedCount;
    private java.util.List<DailyTrendDto> dailyTrends;

    public RmaDashboardStatsDto() {
    }

    public RmaDashboardStatsDto(long totalRequests, long totalItems, long repairedCount, long unrepairedCount,
            java.util.List<DailyTrendDto> dailyTrends) {
        this.totalRequests = totalRequests;
        this.totalItems = totalItems;
        this.repairedCount = repairedCount;
        this.unrepairedCount = unrepairedCount;
        this.dailyTrends = dailyTrends;
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

    public java.util.List<DailyTrendDto> getDailyTrends() {
        return dailyTrends;
    }

    public void setDailyTrends(java.util.List<DailyTrendDto> dailyTrends) {
        this.dailyTrends = dailyTrends;
    }
}
