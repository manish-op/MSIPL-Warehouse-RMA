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
        dto.setRepairStatus(item.getRepairStatus());
        dto.setDepotReturnDcNo(item.getDepotReturnDcNo());
        dto.setDispatchTo(item.getDispatchTo());
        return dto;
    }

    public String getDepotStage() {
        return depotStage;
    }

    public void setDepotStage(String depotStage) {
        this.depotStage = depotStage;
    }

    private String depotStage;
    // repairStatus is generally inherited or defined elsewhere if Duplicate, but
    // here we just need to avoid redeclaration.
    // If it was already there, we keep it but ensure no dupes.
    // The lint said duplicate, so I will remove this one if it exists elsewhere.
    // Actually, looking at the file (Step 1306/1320), I don't see another
    // definition in the snippet,
    // BUT the lint says there is one at line 154 (which is likely the original one
    // I missed).
    // I will just remove it from here.
    private String depotReturnDcNo;
    private String dispatchTo;

    public Long getId() {
        return id;
    }

    public String getDepotReturnDcNo() {
        return depotReturnDcNo;
    }

    public void setDepotReturnDcNo(String depotReturnDcNo) {
        this.depotReturnDcNo = depotReturnDcNo;
    }

    public String getDispatchTo() {
        return dispatchTo;
    }

    public void setDispatchTo(String dispatchTo) {
        this.dispatchTo = dispatchTo;
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

    private String repairStatus;

    public String getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus) {
        this.repairStatus = repairStatus;
    }
}
