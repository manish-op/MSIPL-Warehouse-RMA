package com.serverManagement.server.management.entity.rma;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity to track generated RMA Inward Gatepasses
 */
@Table(name = "rma_inward_gatepass")
@Entity
public class RmaInwardGatepassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gatepass_number", unique = true, nullable = false, length = 50)
    private String gatepassNumber; // Auto-generated: IGP-XXXX

    @ManyToOne
    @JoinColumn(name = "rma_request_id", nullable = false)
    private RmaRequestEntity rmaRequest;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "supplier_address", length = 2000)
    private String supplierAddress;

    @Column(name = "dc_invoice_no", length = 100)
    private String dcInvoiceNo; // Optional

    @Column(name = "generated_date", nullable = false)
    private ZonedDateTime generatedDate;

    @Column(name = "generated_by_email")
    private String generatedByEmail;

    @Column(name = "generated_by_name")
    private String generatedByName;

    @Column(name = "item_count")
    private Integer itemCount;

    // Constructors
    public RmaInwardGatepassEntity() {
        super();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGatepassNumber() {
        return gatepassNumber;
    }

    public void setGatepassNumber(String gatepassNumber) {
        this.gatepassNumber = gatepassNumber;
    }

    public RmaRequestEntity getRmaRequest() {
        return rmaRequest;
    }

    public void setRmaRequest(RmaRequestEntity rmaRequest) {
        this.rmaRequest = rmaRequest;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }

    public String getDcInvoiceNo() {
        return dcInvoiceNo;
    }

    public void setDcInvoiceNo(String dcInvoiceNo) {
        this.dcInvoiceNo = dcInvoiceNo;
    }

    public ZonedDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(ZonedDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getGeneratedByEmail() {
        return generatedByEmail;
    }

    public void setGeneratedByEmail(String generatedByEmail) {
        this.generatedByEmail = generatedByEmail;
    }

    public String getGeneratedByName() {
        return generatedByName;
    }

    public void setGeneratedByName(String generatedByName) {
        this.generatedByName = generatedByName;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    @Override
    public String toString() {
        return "RmaInwardGatepassEntity [id=" + id + ", gatepassNumber=" + gatepassNumber
                + ", supplierName=" + supplierName + ", generatedDate=" + generatedDate
                + ", itemCount=" + itemCount + "]";
    }
}
