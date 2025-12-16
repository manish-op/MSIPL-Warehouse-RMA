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
 * Entity to track generated RMA Outward Gatepasses (Local Repair)
 */
@Table(name = "rma_outward_gatepass")
@Entity
public class RmaOutwardGatepassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gatepass_number", unique = true, nullable = false, length = 50)
    private String gatepassNumber; // Auto-generated: OGP-XXXX

    @ManyToOne
    @JoinColumn(name = "rma_request_id", nullable = true)
    private RmaRequestEntity rmaRequest;

    @Column(name = "consignee_name", nullable = false)
    private String consigneeName;

    @Column(name = "consignee_address", length = 2000)
    private String consigneeAddress;

    @Column(name = "generated_date", nullable = false)
    private ZonedDateTime generatedDate;

    @Column(name = "generated_by_email")
    private String generatedByEmail;

    @Column(name = "generated_by_name")
    private String generatedByName;

    @Column(name = "item_count")
    private Integer itemCount;

    // Constructors
    public RmaOutwardGatepassEntity() {
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

    public String getConsigneeName() {
        return consigneeName;
    }

    public void setConsigneeName(String consigneeName) {
        this.consigneeName = consigneeName;
    }

    public String getConsigneeAddress() {
        return consigneeAddress;
    }

    public void setConsigneeAddress(String consigneeAddress) {
        this.consigneeAddress = consigneeAddress;
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
        return "RmaOutwardGatepassEntity [id=" + id + ", gatepassNumber=" + gatepassNumber
                + ", consigneeName=" + consigneeName + ", generatedDate=" + generatedDate
                + ", itemCount=" + itemCount + "]";
    }
}
