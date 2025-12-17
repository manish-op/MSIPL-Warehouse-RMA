package com.serverManagement.server.management.dto.rma;

public class RmaDashboardStatsDto {

    private long totalRequests;
    private long totalItems;
    private long repairedCount;
    private long unrepairedCount;
    private java.util.List<DailyTrendDto> trendData;

    public RmaDashboardStatsDto() {
    }

    public RmaDashboardStatsDto(long totalRequests, long totalItems, long repairedCount, long unrepairedCount,
            java.util.List<DailyTrendDto> trendData) {
        this.totalRequests = totalRequests;
        this.totalItems = totalItems;
        this.repairedCount = repairedCount;
        this.unrepairedCount = unrepairedCount;
        this.trendData = trendData;
    }

    // Keep old constructor for backward compatibility
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

    public java.util.List<DailyTrendDto> getTrendData() {
        return trendData;
    }

    public void setTrendData(java.util.List<DailyTrendDto> trendData) {
        this.trendData = trendData;
    }

    public static class DailyTrendDto {
        private String name;
        private long requests;

        public DailyTrendDto() {
        }

        public DailyTrendDto(String name, long requests) {
            this.name = name;
            this.requests = requests;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getRequests() {
            return requests;
        }

        public void setRequests(long requests) {
            this.requests = requests;
        }
    }
}
