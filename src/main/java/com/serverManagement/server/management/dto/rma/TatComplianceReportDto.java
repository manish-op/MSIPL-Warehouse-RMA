package com.serverManagement.server.management.dto.rma;

import java.time.ZonedDateTime;

/**
 * DTO for TAT Compliance Report - shows customer-wise TAT compliance data
 */
public class TatComplianceReportDto {

    private String companyName; // Customer name
    private Integer defaultTat; // Customer's default TAT (from CustomerEntity)
    private Long totalRequests; // Total requests for this customer
    private Long requestsWithTat; // Requests that had TAT defined
    private Long completedWithinTat; // Closed within TAT deadline
    private Long completedAfterTat; // Closed but breached TAT (overdue)
    private Long stillOpen; // Not yet closed
    private Long onTrack; // Open and on track
    private Long atRisk; // Open and at risk
    private Long breached; // Open and breached
    private Double complianceRate; // % completed within TAT (of closed requests)
    private ZonedDateTime oldestOpenDueDate; // Earliest due date among open requests

    public TatComplianceReportDto() {
    }

    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getDefaultTat() {
        return defaultTat;
    }

    public void setDefaultTat(Integer defaultTat) {
        this.defaultTat = defaultTat;
    }

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Long getRequestsWithTat() {
        return requestsWithTat;
    }

    public void setRequestsWithTat(Long requestsWithTat) {
        this.requestsWithTat = requestsWithTat;
    }

    public Long getCompletedWithinTat() {
        return completedWithinTat;
    }

    public void setCompletedWithinTat(Long completedWithinTat) {
        this.completedWithinTat = completedWithinTat;
    }

    public Long getCompletedAfterTat() {
        return completedAfterTat;
    }

    public void setCompletedAfterTat(Long completedAfterTat) {
        this.completedAfterTat = completedAfterTat;
    }

    public Long getStillOpen() {
        return stillOpen;
    }

    public void setStillOpen(Long stillOpen) {
        this.stillOpen = stillOpen;
    }

    public Long getOnTrack() {
        return onTrack;
    }

    public void setOnTrack(Long onTrack) {
        this.onTrack = onTrack;
    }

    public Long getAtRisk() {
        return atRisk;
    }

    public void setAtRisk(Long atRisk) {
        this.atRisk = atRisk;
    }

    public Long getBreached() {
        return breached;
    }

    public void setBreached(Long breached) {
        this.breached = breached;
    }

    public Double getComplianceRate() {
        return complianceRate;
    }

    public void setComplianceRate(Double complianceRate) {
        this.complianceRate = complianceRate;
    }

    public ZonedDateTime getOldestOpenDueDate() {
        return oldestOpenDueDate;
    }

    public void setOldestOpenDueDate(ZonedDateTime oldestOpenDueDate) {
        this.oldestOpenDueDate = oldestOpenDueDate;
    }
}
