package com.serverManagement.server.management.entity.rma;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;

@Table(name = "rma_item")
@Entity
public class RmaItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Product Details
    @Column(nullable = false)
    private String product;
    private String model; // Model No./Part No.
    @Column(nullable = false)
    private String serialNo;

    // Auto-generated RMA Number (Deprecated: Now stored at request level)
    // Kept for backward compatibility with existing database records
    @Column(nullable = true)
    @Deprecated
    private String rmaNo;

    // Fault Information
    @Column(nullable = false, length = 2000)
    private String faultDescription;

    // Technical Details
    private String codeplug; // DEFAULT/CUSTOMER CODEPLUG
    private String flashCode;

    @Column(name = "repair_status")
    private String repairStatus; // WARR/OWA/AMC/SFS

    private String invoiceNo; // For accessory
    private String dateCode; // For accessory

    @Column(nullable = false)
    private String fmUlatex; // FM/UL/ATEX - Mandatory Y/N

    private String encryption; // Tetra/Astro
    private String firmwareVersion; // Tetra/Astro
    private String lowerFirmwareVersion; // Mototrbo

    @Column(length = 1000)
    private String remarks;

    @Column(name = "partial_shipment")
    private String partialShipment;

    // Assignment tracking
    @Column(name = "assigned_to_email")
    private String assignedToEmail;

    @Column(name = "assigned_to_name")
    private String assignedToName;

    @Column(name = "assigned_date")
    private ZonedDateTime assignedDate;

    // Repair completion tracking
    @Column(name = "repaired_by_email")
    private String repairedByEmail;

    @Column(name = "repaired_by_name")
    private String repairedByName;

    @Column(name = "repaired_date")
    private ZonedDateTime repairedDate;

    @Column(name = "repair_remarks", length = 2000)
    private String repairRemarks;

    @Column(name = "issue_fixed", length = 2000)
    private String issueFixed;

    @Column(name = "repair_type")
    private String repairType; // for local or depot

    @Column(name = "local_stage")
    private String localStage; // unassigned,under repair or dispatched

    @Column(name = "depot_stage")
    private String depotStage; // for depot flow: pending dispatch to depot or In transit to depot

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "depot_dispatch_id")
    private DepotDispatchEntity depotDispatch;

    // @Column(name = "dc_no")
    // private String dcNo;

    // @Column(name = "eway_bill_no")
    // private String ewayBillNo;

    // @Column(name = "dispatch_date")
    // private ZonedDateTime dispatchDate;

    // @Column(name = "courier_name")
    // private String courierName;

    // @Column(name = "tracking_no")
    // private String trackingNo;

    // Relationship with RMA Request
    @ManyToOne
    @JoinColumn(name = "rma_request_id", nullable = false)
    private RmaRequestEntity rmaRequest;

    // Constructors
    public RmaItemEntity() {
        super();
    }

    public RmaItemEntity(Long id, String product, String model, String serialNo, String rmaNo, String faultDescription,
            String codeplug, String flashCode, String repairStatus, String invoiceNo, String dateCode,
            String fmUlatex, String encryption, String firmwareVersion, String lowerFirmwareVersion,
            String remarks, RmaRequestEntity rmaRequest) {
        super();
        this.id = id;
        this.product = product;
        this.model = model;
        this.serialNo = serialNo;
        this.rmaNo = rmaNo;
        this.faultDescription = faultDescription;
        this.codeplug = codeplug;
        this.flashCode = flashCode;
        this.repairStatus = repairStatus;
        this.invoiceNo = invoiceNo;
        this.dateCode = dateCode;
        this.fmUlatex = fmUlatex;
        this.encryption = encryption;
        this.firmwareVersion = firmwareVersion;
        this.lowerFirmwareVersion = lowerFirmwareVersion;
        this.remarks = remarks;
        this.rmaRequest = rmaRequest;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getRmaNo() {
        return rmaNo;
    }

    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    public String getCodeplug() {
        return codeplug;
    }

    public void setCodeplug(String codeplug) {
        this.codeplug = codeplug;
    }

    public String getFlashCode() {
        return flashCode;
    }

    public void setFlashCode(String flashCode) {
        this.flashCode = flashCode;
    }

    public String getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus) {
        this.repairStatus = repairStatus;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }

    public String getFmUlatex() {
        return fmUlatex;
    }

    public void setFmUlatex(String fmUlatex) {
        this.fmUlatex = fmUlatex;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getLowerFirmwareVersion() {
        return lowerFirmwareVersion;
    }

    public void setLowerFirmwareVersion(String lowerFirmwareVersion) {
        this.lowerFirmwareVersion = lowerFirmwareVersion;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPartialShipment() {
        return partialShipment;
    }

    public void setPartialShipment(String partialShipment) {
        this.partialShipment = partialShipment;
    }

    public RmaRequestEntity getRmaRequest() {
        return rmaRequest;
    }

    public void setRmaRequest(RmaRequestEntity rmaRequest) {
        this.rmaRequest = rmaRequest;
    }

    // Assignment tracking getters and setters
    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public ZonedDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(ZonedDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    // Repair completion getters and setters
    public String getRepairedByEmail() {
        return repairedByEmail;
    }

    public void setRepairedByEmail(String repairedByEmail) {
        this.repairedByEmail = repairedByEmail;
    }

    public String getRepairedByName() {
        return repairedByName;
    }

    public void setRepairedByName(String repairedByName) {
        this.repairedByName = repairedByName;
    }

    public ZonedDateTime getRepairedDate() {
        return repairedDate;
    }

    public void setRepairedDate(ZonedDateTime repairedDate) {
        this.repairedDate = repairedDate;
    }

    public String getRepairRemarks() {
        return repairRemarks;
    }

    public void setRepairRemarks(String repairRemarks) {
        this.repairRemarks = repairRemarks;
    }

    public String getIssueFixed() {
        return issueFixed;
    }

    public void setIssueFixed(String issueFixed) {
        this.issueFixed = issueFixed;
    }

    // for depot dispatch page
    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
    }

    public String getDepotStage() {
        return depotStage;
    }

    public void setDepotStage(String depotStage) {
        this.depotStage = depotStage;
    }

    public String getLocalStage() {
        return localStage;
    }

    public void setLocalStage(String localStage) {
        this.localStage = localStage;
    }

    public DepotDispatchEntity getDepotDispatch() {
        return depotDispatch;
    }

    public void setDepotDispatch(DepotDispatchEntity depotDispatch) {
        this.depotDispatch = depotDispatch;
    }

    public String getDcNo() {
        return depotDispatch != null ? depotDispatch.getDcNo() : null;
    }

    public void setDcNo(String dcNo) {
        // this.dcNo = dcNo; // Old
        if (this.depotDispatch == null)
            this.depotDispatch = new DepotDispatchEntity();
        this.depotDispatch.setDcNo(dcNo);
    }

    public String getEwayBillNo() {
        return depotDispatch != null ? depotDispatch.getEwayBillNo() : null;
    }

    public void setEwayBillNo(String ewayBillNo) {
        if (this.depotDispatch == null)
            this.depotDispatch = new DepotDispatchEntity();
        this.depotDispatch.setEwayBillNo(ewayBillNo);
    }

    public ZonedDateTime getDispatchDate() {
        return depotDispatch != null ? depotDispatch.getDispatchDate() : null;
    }

    public void setDispatchDate(ZonedDateTime dispatchDate) {
        if (this.depotDispatch == null)
            this.depotDispatch = new DepotDispatchEntity();
        this.depotDispatch.setDispatchDate(dispatchDate);
    }

    public String getCourierName() {
        return depotDispatch != null ? depotDispatch.getCourierName() : null;
    }

    public void setCourierName(String courierName) {
        if (this.depotDispatch == null)
            this.depotDispatch = new DepotDispatchEntity();
        this.depotDispatch.setCourierName(courierName);
    }

    public String getTrackingNo() {
        return depotDispatch != null ? depotDispatch.getTrackingNo() : null;
    }

    public void setTrackingNo(String trackingNo) {
        if (this.depotDispatch == null)
            this.depotDispatch = new DepotDispatchEntity();
        this.depotDispatch.setTrackingNo(trackingNo);
    }

    @Override
    public String toString() {
        return "RmaItemEntity [id=" + id + ", product=" + product + ", model=" + model + ", serialNo=" + serialNo
                + ", rmaNo=" + rmaNo + ", faultDescription=" + faultDescription + ", codeplug=" + codeplug
                + ", flashCode=" + flashCode + ", repairStatus=" + repairStatus + ", invoiceNo=" + invoiceNo
                + ", dateCode=" + dateCode + ", fmUlatex=" + fmUlatex + ", encryption=" + encryption
                + ", firmwareVersion=" + firmwareVersion + ", lowerFirmwareVersion=" + lowerFirmwareVersion
                + ", remarks=" + remarks + ", assignedToEmail=" + assignedToEmail
                + ", assignedToName=" + assignedToName + "]";
    }
}
