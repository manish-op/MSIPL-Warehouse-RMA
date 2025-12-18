package com.serverManagement.server.management.dto.itemRepairDetails;

import java.util.List;

public class DashboardSummaryDto {
    private long totalItems;
    private long availableCount;
    private long issuedCount;
    private long repairingCount;
    private List<RegionCountDto> regionCounts;

    public DashboardSummaryDto() {
    }

    public DashboardSummaryDto(long totalItems, List<RegionCountDto> regionCounts) {
        this.totalItems = totalItems;
        this.regionCounts = regionCounts;
    }

    public DashboardSummaryDto(long totalItems, long availableCount, long issuedCount, long repairingCount,
            List<RegionCountDto> regionCounts) {
        this.totalItems = totalItems;
        this.availableCount = availableCount;
        this.issuedCount = issuedCount;
        this.repairingCount = repairingCount;
        this.regionCounts = regionCounts;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(long availableCount) {
        this.availableCount = availableCount;
    }

    public long getIssuedCount() {
        return issuedCount;
    }

    public void setIssuedCount(long issuedCount) {
        this.issuedCount = issuedCount;
    }

    public long getRepairingCount() {
        return repairingCount;
    }

    public void setRepairingCount(long repairingCount) {
        this.repairingCount = repairingCount;
    }

    public List<RegionCountDto> getRegionCounts() {
        return regionCounts;
    }

    public void setRegionCounts(List<RegionCountDto> regionCounts) {
        this.regionCounts = regionCounts;
    }
}
