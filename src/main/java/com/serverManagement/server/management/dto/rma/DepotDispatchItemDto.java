package com.serverManagement.server.management.dto.rma;

import com.serverManagement.server.management.entity.rma.RmaItemEntity;

public class DepotDispatchItemDto {
    private Long id;
    private String rmaNo;
    private String product;
    private String serialNo;
    private String model;
    private String faultDescription;
    private String companyName;
    private String repairType;
    private String dcNo;
    private String ewayBillNo;

    private String itemRmaNo;

    public static DepotDispatchItemDto fromEntity(RmaItemEntity item) {
        DepotDispatchItemDto dto = new DepotDispatchItemDto();
        dto.setId(item.getId());
        dto.setRmaNo(item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
        dto.setItemRmaNo(item.getRmaNo()); // Map item-level RMA no
        dto.setProduct(item.getProduct());
        String displaySerial = item.getSerialNo();
        if (displaySerial != null && displaySerial.startsWith("NA-")) {
            displaySerial = "N/A";
        }
        dto.setSerialNo(displaySerial);
        dto.setModel(item.getModel());
        dto.setFaultDescription(item.getFaultDescription());
        dto.setCompanyName(item.getRmaRequest() != null ? item.getRmaRequest().getCompanyName() : null);
        dto.setRepairType(item.getRepairType());
        dto.setDcNo(item.getDcNo());
        dto.setEwayBillNo(item.getEwayBillNo());
        dto.setDepotStage(item.getDepotStage());
        return dto;
    }

    public String getDepotStage() {
        return depotStage;
    }

    public void setDepotStage(String depotStage) {
        this.depotStage = depotStage;
    }

    private String depotStage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRmaNo() {
        return rmaNo;
    }

    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    public String getItemRmaNo() {
        return itemRmaNo;
    }

    public void setItemRmaNo(String itemRmaNo) {
        this.itemRmaNo = itemRmaNo;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
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
}
