package com.serverManagement.server.management.entity.slaproject;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_core")
public class ProjectCore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;
    @Column(name = "system_id", unique = true, nullable = false)
    private String systemId;
    @Column(name = "project_name")
    private String projectName;

    private String region;

    private String category;

    @Column(name = "partner_name")
    private String partnerName;
    @Column(name = "customer_number")
    private String customerNumber;
    @Column(name = "sfdc_number")
    private String sfdcNumber;
    @Column(name = "po_value")
    private BigDecimal poValue;
    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;
    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;
    @Column(name = "amc_status")
    private String amcStatus;

    // --- Relationship ---
    // 'mappedBy' tells Hibernate that the Foreign Key is in the other class
    // CascadeType.ALL means if you save/delete ProjectCore, the Details are
    // saved/deleted too.
    @com.fasterxml.jackson.annotation.JsonManagedReference
    @OneToOne(mappedBy = "projectCore", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ServiceSlaDetails serviceSlaDetails;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getSfdcNumber() {
        return sfdcNumber;
    }

    public void setSfdcNumber(String sfdcNumber) {
        this.sfdcNumber = sfdcNumber;
    }

    public BigDecimal getPoValue() {
        return poValue;
    }

    public void setPoValue(BigDecimal poValue) {
        this.poValue = poValue;
    }

    public LocalDate getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public LocalDate getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public String getAmcStatus() {
        return amcStatus;
    }

    public void setAmcStatus(String amcStatus) {
        this.amcStatus = amcStatus;
    }

    public ServiceSlaDetails getServiceSlaDetails() {
        return serviceSlaDetails;
    }

    public void setServiceSlaDetails(ServiceSlaDetails serviceSlaDetails) {
        this.serviceSlaDetails = serviceSlaDetails;
    }

}
