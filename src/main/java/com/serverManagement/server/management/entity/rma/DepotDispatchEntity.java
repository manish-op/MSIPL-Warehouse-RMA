package com.serverManagement.server.management.entity.rma;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "depot_dispatch")
public class DepotDispatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dc_no")
    private String dcNo;

    @Column(name = "eway_bill_no")
    private String ewayBillNo;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "tracking_no")
    private String trackingNo;

    @Column(name = "dispatch_date")
    private ZonedDateTime dispatchDate;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }

    public DepotDispatchEntity() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ZonedDateTime getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(ZonedDateTime dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
