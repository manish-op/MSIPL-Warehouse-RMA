package com.serverManagement.server.management.request.itemDetails;
import java.util.List;

public class StockTransferRequest {
    private List<Long> itemIds;

    // The region ID the items are coming FROM
    private Long sourceRegionId;

    // The region ID the items are going TO
    private Long destinationRegionId;



    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }

    public Long getSourceRegionId() {
        return sourceRegionId;
    }

    public void setSourceRegionId(Long sourceRegionId) {
        this.sourceRegionId = sourceRegionId;
    }

    public Long getDestinationRegionId() {
        return destinationRegionId;
    }

    public void setDestinationRegionId(Long destinationRegionId) {
        this.destinationRegionId = destinationRegionId;
    }

}
