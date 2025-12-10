package com.serverManagement.server.management.dto.rma;

public class RmaDashboardStatsDto {

    private long totalRequests;
    private long totalItems;
    private long repairedCount;
    private long unrepairedCount;

    public RmaDashboardStatsDto() {
    }

    public RmaDashboardStatsDto(long totalRequests, long totalItems, long repairedCount, long unrepairedCount) {
        this.totalRequests = totalRequests;
        this.totalItems = totalItems;
        this.repairedCount = repairedCount;
        this.unrepairedCount = unrepairedCount;
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
}
