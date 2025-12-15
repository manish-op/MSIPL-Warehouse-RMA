package com.serverManagement.server.management.dto.rma;

import java.time.LocalDate;
import java.util.List;

public class DepotDispatchRequest {
    private String rmaNo;
    private List<Long> itemIds;
    // Dispatch Fields
    private String dcNo;
    private String ewayBillNo;
    private String courierName;
    private String trackingNo;
    private LocalDate dispatchDate;
    private String remarks;
    // Delivery Challan Generation Fields
    private String consigneeName;
    private String consigneeAddress;
    private String gstIn;
    private Integer boxes;
    private String dimensions;
    private String weight;
    private String modeOfShipment;
    private String transporterName;
    private String transporterId;
    private List<DcItemDto> items;

    // Inner DTO for DC Items
    public static class DcItemDto {
        private String slNo;
        private String product;
        private String model;
        private String serialNo;
        private String rate; // Needed for value calculation

        public String getSlNo() {
            return slNo;
        }

        public void setSlNo(String slNo) {
            this.slNo = slNo;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(String serialNo) {
            this.serialNo = serialNo;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }
    }

    // Getters and Setters
    public String getRmaNo() {
        return rmaNo;
    }

    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }

    public String getDcNo() {
        return dcNo;
    }

    public void setDcNo(String dcNo) {
        this.dcNo = dcNo;
    }

    public String getEwayBillNo() {
        return ewayBillNo;
    }

    public void setEwayBillNo(String ewayBillNo) {
        this.ewayBillNo = ewayBillNo;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public LocalDate getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(LocalDate dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getConsigneeName() {
        return consigneeName;
    }

    public void setConsigneeName(String consigneeName) {
        this.consigneeName = consigneeName;
    }

    public String getConsigneeAddress() {
        return consigneeAddress;
    }

    public void setConsigneeAddress(String consigneeAddress) {
        this.consigneeAddress = consigneeAddress;
    }

    public String getGstIn() {
        return gstIn;
    }

    public void setGstIn(String gstIn) {
        this.gstIn = gstIn;
    }

    public Integer getBoxes() {
        return boxes;
    }

    public void setBoxes(Integer boxes) {
        this.boxes = boxes;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getModeOfShipment() {
        return modeOfShipment;
    }

    public void setModeOfShipment(String modeOfShipment) {
        this.modeOfShipment = modeOfShipment;
    }

    public String getTransporterName() {
        return transporterName;
    }

    public void setTransporterName(String transporterName) {
        this.transporterName = transporterName;
    }

    public String getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(String transporterId) {
        this.transporterId = transporterId;
    }

    public List<DcItemDto> getItems() {
        return items;
    }

    public void setItems(List<DcItemDto> items) {
        this.items = items;
    }
}
