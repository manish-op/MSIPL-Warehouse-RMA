package com.serverManagement.server.management.entity.itemDetails;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

/**
 * Entity for warehouse audit logs - tracks all item changes
 */
@Entity
@Table(name = "warehouse_audit_log")
public class WarehouseAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Item reference
    private Long itemId;
    private String serialNo;

    // Action details
    private String action; // CREATED, UPDATED, ISSUED, RETURNED, REGION_CHANGED, STATUS_CHANGED
    private String fieldChanged; // Which field was changed (null for CREATED)
    private String oldValue;
    private String newValue;

    // Who performed the action
    private String performedByEmail;
    private String performedByName;

    // When and from where
    private ZonedDateTime performedAt;
    private String ipAddress;

    // Additional context
    private String remarks;
    private String region;

    // Constructors
    public WarehouseAuditLogEntity() {
        this.performedAt = ZonedDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFieldChanged() {
        return fieldChanged;
    }

    public void setFieldChanged(String fieldChanged) {
        this.fieldChanged = fieldChanged;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getPerformedByEmail() {
        return performedByEmail;
    }

    public void setPerformedByEmail(String performedByEmail) {
        this.performedByEmail = performedByEmail;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    public ZonedDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(ZonedDateTime performedAt) {
        this.performedAt = performedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
