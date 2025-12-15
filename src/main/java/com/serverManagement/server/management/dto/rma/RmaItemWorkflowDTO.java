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
    private String itemRmaNo; // Item-level RMA number (distinct from request number)
    private String issueFixed; // Description of issue that was fixed
    private String companyName; // Customer company name from parent RMA request
    private ZonedDateTime receivedDate; // Date when RMA request was created/received
    private String repairType;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
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

    public String getItemRmaNo() {
        return itemRmaNo;
    }

    public void setItemRmaNo(String itemRmaNo) {
        this.itemRmaNo = itemRmaNo;
    }

    public String getIssueFixed() {
        return issueFixed;
    }

    public void setIssueFixed(String issueFixed) {
        this.issueFixed = issueFixed;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public ZonedDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(ZonedDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }
}
