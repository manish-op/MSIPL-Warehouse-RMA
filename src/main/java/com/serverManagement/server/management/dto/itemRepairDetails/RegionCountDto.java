package com.serverManagement.server.management.dto.itemRepairDetails;

public class RegionCountDto {
    private String region;
    private long count;

    public RegionCountDto() {}
    public RegionCountDto(String region, long count) {
        this.region = region;
        this.count = count;
    }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
