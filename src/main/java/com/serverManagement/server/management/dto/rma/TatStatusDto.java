package com.serverManagement.server.management.dto.rma;

import java.time.ZonedDateTime;

/**
 * DTO to expose TAT (Turn Around Time) status for RMA requests.
 * Used for visual deadline tracking in the UI.
 */
public class TatStatusDto {

    private Integer tat; // Original TAT in days
    private ZonedDateTime dueDate; // Calculated deadline
    private Integer daysRemaining; // Days until due (negative if overdue)
    private String status; // "ON_TRACK", "AT_RISK", "BREACHED"

    public TatStatusDto() {
    }

    public TatStatusDto(Integer tat, ZonedDateTime dueDate, Integer daysRemaining, String status) {
        this.tat = tat;
        this.dueDate = dueDate;
        this.daysRemaining = daysRemaining;
        this.status = status;
    }

    // Getters and Setters
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

    public Integer getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Integer daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
