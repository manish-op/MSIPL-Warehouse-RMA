package com.serverManagement.server.management.dto.itemRepairDetails;

import java.util.List;

public class DashboardSummaryDto {
    private  long totalItems;
    private List<RegionCountDto> regionCounts;

    public DashboardSummaryDto() {}

    public DashboardSummaryDto(long totalItems, List<RegionCountDto> regionCounts) {
        this.totalItems = totalItems;
        this.regionCounts = regionCounts;
    }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public List<RegionCountDto> getRegionCounts() { return regionCounts; }
    public void setRegionCounts(List<RegionCountDto> regionCounts) {
        this.regionCounts = regionCounts; }
}


