package com.serverManagement.server.management.dto.rma.depot;

import java.time.LocalDate;
import java.util.List;

public class DepotDispatchRequest {
    private String rmaNo;
    private List<Long> itemIds;
    private LocalDate dispatchDate;
    private String courierName;
    private String trackingNo;
    private String dcNo;
    private String ewayBillNo;
    private String remarks;
    private String dispatchMode;
    private String handlerName;
    private String handlerContact;

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

    public LocalDate getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(LocalDate dispatchDate) {
        this.dispatchDate = dispatchDate;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDispatchMode() {
        return dispatchMode;
    }

    public void setDispatchMode(String dispatchMode) {
        this.dispatchMode = dispatchMode;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerContact() {
        return handlerContact;
    }

    public void setHandlerContact(String handlerContact) {
        this.handlerContact = handlerContact;
    }

    private String repairStatus;

    public String getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus) {
        this.repairStatus = repairStatus;
    }

    private String dispatchTo; // GURGAON or CUSTOMER

    public String getDispatchTo() {
        return dispatchTo;
    }

    public void setDispatchTo(String dispatchTo) {
        this.dispatchTo = dispatchTo;
    }
}
