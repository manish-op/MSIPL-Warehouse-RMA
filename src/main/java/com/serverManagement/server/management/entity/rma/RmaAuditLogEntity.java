package com.serverManagement.server.management.entity.rma;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity for tracking all status changes and actions on RMA items.
 * Provides audit trail for security and compliance purposes.
 */
@Entity
@Table(name = "rma_audit_log")
public class RmaAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long rmaItemId;

    @Column(name = "rma_no")
    private String rmaNo;

    @Column(nullable = false)
    private String action; // e.g., "CREATED", "ASSIGNED", "STATUS_CHANGED", "REPAIRED"

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    @Column(name = "performed_by_email", nullable = false)
    private String performedByEmail;

    @Column(name = "performed_by_name")
    private String performedByName;

    @Column(name = "performed_at", nullable = false)
    private ZonedDateTime performedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(length = 1000)
    private String remarks;

    // Constructors
    public RmaAuditLogEntity() {
        this.performedAt = ZonedDateTime.now();
    }

    public RmaAuditLogEntity(Long rmaItemId, String rmaNo, String action, String oldValue,
            String newValue, String performedByEmail, String performedByName) {
        this.rmaItemId = rmaItemId;
        this.rmaNo = rmaNo;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.performedByEmail = performedByEmail;
        this.performedByName = performedByName;
        this.performedAt = ZonedDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRmaItemId() {
        return rmaItemId;
    }

    public void setRmaItemId(Long rmaItemId) {
        this.rmaItemId = rmaItemId;
    }

    public String getRmaNo() {
        return rmaNo;
    }

    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
}
