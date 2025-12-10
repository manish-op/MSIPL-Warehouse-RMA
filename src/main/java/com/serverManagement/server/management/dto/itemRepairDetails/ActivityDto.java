package com.serverManagement.server.management.dto.itemRepairDetails;

import java.time.ZonedDateTime;
import java.util.Map;

public class ActivityDto {
    private Long id;
    private String serialNo;
    private String updatedByEmail;
    private String updatedByName;
    private String remark;
    private String region;
    private ZonedDateTime updateDate;
    private Map<String, Object> details; // optional: changed fields

    // Additional audit fields
    private String action; // CREATED, UPDATED, ISSUED, RETURNED, REGION_CHANGED
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String keyword;
    private String subKeyword;
    private String availabilityStatus;
    private String itemStatus;

    public ActivityDto() {
    }

    public ActivityDto(Long id, String serialNo, String updatedByEmail, String remark, String region,
            ZonedDateTime updateDate) {
        this.id = id;
        this.serialNo = serialNo;
        this.updatedByEmail = updatedByEmail;
        this.remark = remark;
        this.region = region;
        this.updateDate = updateDate;
    }

    // Getters / Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getUpdatedByEmail() {
        return updatedByEmail;
    }

    public void setUpdatedByEmail(String updatedByEmail) {
        this.updatedByEmail = updatedByEmail;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public ZonedDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(ZonedDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    // Additional getters/setters
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSubKeyword() {
        return subKeyword;
    }

    public void setSubKeyword(String subKeyword) {
        this.subKeyword = subKeyword;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }
}
