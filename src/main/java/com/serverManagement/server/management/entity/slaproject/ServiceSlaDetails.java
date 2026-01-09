package com.serverManagement.server.management.entity.slaproject;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_sla_details")
public class ServiceSlaDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    @com.fasterxml.jackson.annotation.JsonBackReference
    @OneToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private ProjectCore projectCore;

    @Column(name = "system_version")
    private String systemVersion;

    @Column(name = "frequency_band")
    private String frequencyBand;

    @Column(name = "encryption_type")
    private String encryptionType;

    // --- Operations & Support ---
    @Column(name = "spoc_contact")
    private String spocContact;

    @Column(name = "invoice_billing_cycle")
    private String invoiceBillingCycle;

    private String slaScope;
    @Column(name = "repair_tat")
    private String repairTat; // Turnaround Time

    @Column(name = "pmi_schedule")
    private String pmiSchedule; // Preventive Maintenance

    @Column(name = "service_report_freq")
    private String serviceReportFrequency;

    @Column(name = "dlp_warranty_start")
    private LocalDate dlpWarrantyStartDate;

    @Column(name = "dlp_warranty_end")
    private LocalDate dlpWarrantyEndDate;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectCore getProjectCore() {
        return projectCore;
    }

    public void setProjectCore(ProjectCore projectCore) {
        this.projectCore = projectCore;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getFrequencyBand() {
        return frequencyBand;
    }

    public void setFrequencyBand(String frequencyBand) {
        this.frequencyBand = frequencyBand;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public String getSpocContact() {
        return spocContact;
    }

    public void setSpocContact(String spocContact) {
        this.spocContact = spocContact;
    }

    public String getInvoiceBillingCycle() {
        return invoiceBillingCycle;
    }

    public void setInvoiceBillingCycle(String invoiceBillingCycle) {
        this.invoiceBillingCycle = invoiceBillingCycle;
    }

    public String getSlaScope() {
        return slaScope;
    }

    public void setSlaScope(String slaScope) {
        this.slaScope = slaScope;
    }

    public String getRepairTat() {
        return repairTat;
    }

    public void setRepairTat(String repairTat) {
        this.repairTat = repairTat;
    }

    public String getPmiSchedule() {
        return pmiSchedule;
    }

    public void setPmiSchedule(String pmiSchedule) {
        this.pmiSchedule = pmiSchedule;
    }

    public String getServiceReportFrequency() {
        return serviceReportFrequency;
    }

    public void setServiceReportFrequency(String serviceReportFrequency) {
        this.serviceReportFrequency = serviceReportFrequency;
    }

    public LocalDate getDlpWarrantyStartDate() {
        return dlpWarrantyStartDate;
    }

    public void setDlpWarrantyStartDate(LocalDate dlpWarrantyStartDate) {
        this.dlpWarrantyStartDate = dlpWarrantyStartDate;
    }

    public LocalDate getDlpWarrantyEndDate() {
        return dlpWarrantyEndDate;
    }

    public void setDlpWarrantyEndDate(LocalDate dlpWarrantyEndDate) {
        this.dlpWarrantyEndDate = dlpWarrantyEndDate;
    }

}
