package com.serverManagement.server.management.dto.rma;

import java.time.ZonedDateTime;

/**
 * DTO for RMA item workflow data - used in assigned/repaired items lists
 */
public class RmaItemWorkflowDTO {
    private Long id;
    private String product;
    private String serialNo;
    private String model;
    private String faultDescription;
    private String repairStatus;
    private String rmaNo;
    private String assignedToEmail;
    private String assignedToName;
    private ZonedDateTime assignedDate;
    private String repairedByEmail;
    private String repairedByName;
    private ZonedDateTime repairedDate;
    private String repairRemarks;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus) {
        this.repairStatus = repairStatus;
    }

    public String getRmaNo() {
        return rmaNo;
    }

    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public ZonedDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(ZonedDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getRepairedByEmail() {
        return repairedByEmail;
    }

    public void setRepairedByEmail(String repairedByEmail) {
        this.repairedByEmail = repairedByEmail;
    }

    public String getRepairedByName() {
        return repairedByName;
    }

    public void setRepairedByName(String repairedByName) {
        this.repairedByName = repairedByName;
    }

    public ZonedDateTime getRepairedDate() {
        return repairedDate;
    }

    public void setRepairedDate(ZonedDateTime repairedDate) {
        this.repairedDate = repairedDate;
    }

    public String getRepairRemarks() {
        return repairRemarks;
    }

    public void setRepairRemarks(String repairRemarks) {
        this.repairRemarks = repairRemarks;
    }
}
