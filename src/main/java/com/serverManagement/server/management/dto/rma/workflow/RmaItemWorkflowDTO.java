package com.serverManagement.server.management.dto.rma.workflow;

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
    private String lastReassignmentReason; // Added for displaying reassignment reason
    private Boolean isDispatched; // Dispatch status flag

    // Dispatch tracking fields
    private String dispatchTo; // CUSTOMER or BANGALORE
    private ZonedDateTime dispatchedDate;
    private String dispatchedByEmail;
    private String dispatchedByName;
    private String dcNo; // Delivery Challan Number
    private String ewayBillNo; // E-Way Bill Number

    // Depot Return Tracking
    private ZonedDateTime depotReturnDispatchDate;
    private ZonedDateTime depotReturnDeliveredDate;

    // TAT from Request (Days)
    private Integer tat;
    // Due Date from Request
    private ZonedDateTime dueDate;

    // Delivery confirmation fields
    private String deliveredTo; // Name of person who received
    private String deliveredBy; // Courier/person who delivered
    private ZonedDateTime deliveryDate;
    private String deliveryNotes;
    private Boolean isDelivered; // Simple flag to check if delivery confirmed
    private String userName; // Name of the creator

    // Pre-fill fields for DC
    private String returnAddress;
    private String gstin;

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLastReassignmentReason() {
        return lastReassignmentReason;
    }

    public void setLastReassignmentReason(String lastReassignmentReason) {
        this.lastReassignmentReason = lastReassignmentReason;
    }

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

    public Boolean getIsDispatched() {
        return isDispatched;
    }

    public void setIsDispatched(Boolean isDispatched) {
        this.isDispatched = isDispatched;
    }

    public String getDispatchTo() {
        return dispatchTo;
    }

    public void setDispatchTo(String dispatchTo) {
        this.dispatchTo = dispatchTo;
    }

    public ZonedDateTime getDispatchedDate() {
        return dispatchedDate;
    }

    public void setDispatchedDate(ZonedDateTime dispatchedDate) {
        this.dispatchedDate = dispatchedDate;
    }

    public String getDispatchedByEmail() {
        return dispatchedByEmail;
    }

    public void setDispatchedByEmail(String dispatchedByEmail) {
        this.dispatchedByEmail = dispatchedByEmail;
    }

    public String getDispatchedByName() {
        return dispatchedByName;
    }

    public void setDispatchedByName(String dispatchedByName) {
        this.dispatchedByName = dispatchedByName;
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

    public String getDeliveredTo() {
        return deliveredTo;
    }

    public void setDeliveredTo(String deliveredTo) {
        this.deliveredTo = deliveredTo;
    }

    public String getDeliveredBy() {
        return deliveredBy;
    }

    public void setDeliveredBy(String deliveredBy) {
        this.deliveredBy = deliveredBy;
    }

    public ZonedDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(ZonedDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public Boolean getIsDelivered() {
        return isDelivered;
    }

    public void setIsDelivered(Boolean isDelivered) {
        this.isDelivered = isDelivered;
    }

    public String getReturnAddress() {
        return returnAddress;
    }

    public void setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public ZonedDateTime getDepotReturnDispatchDate() {
        return depotReturnDispatchDate;
    }

    public void setDepotReturnDispatchDate(ZonedDateTime depotReturnDispatchDate) {
        this.depotReturnDispatchDate = depotReturnDispatchDate;
    }

    public ZonedDateTime getDepotReturnDeliveredDate() {
        return depotReturnDeliveredDate;
    }

    public void setDepotReturnDeliveredDate(ZonedDateTime depotReturnDeliveredDate) {
        this.depotReturnDeliveredDate = depotReturnDeliveredDate;
    }

    public Integer getTat() {
        return tat;
    }

    public void setTat(Integer tat) {
        this.tat = tat;
    }

    public ZonedDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(ZonedDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
