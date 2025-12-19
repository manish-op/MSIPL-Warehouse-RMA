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
    @Column(nullable = false) // Auto-filled with "N/A" for accessories without serial numbers
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

    @Column(name = "last_reassignment_reason", length = 2000)
    private String lastReassignmentReason;

    // ============ DEPOT RETURN TRACKING ============
    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "depot_cycle_closed")
    private Boolean depotCycleClosed = false;

    @Column(name = "depot_return_method")
    private String depotReturnMethod; // HAND or COURIER

    @Column(name = "depot_return_dispatch_date")
    private ZonedDateTime depotReturnDispatchDate;

    @Column(name = "depot_return_courier_name")
    private String depotReturnCourierName;

    @Column(name = "depot_return_tracking_no")
    private String depotReturnTrackingNo;

    @Column(name = "depot_return_handler_name")
    private String depotReturnHandlerName;

    @Column(name = "depot_return_handler_contact")
    private String depotReturnHandlerContact;

    @Column(name = "depot_proof_of_delivery_file_id")
    private String depotProofOfDeliveryFileId;

    @Column(name = "depot_proof_of_delivery_remarks")
    private String depotProofOfDeliveryRemarks;

    @Column(name = "depot_return_delivered_date")
    private ZonedDateTime depotReturnDeliveredDate;

    // ============ DISPATCH TRACKING ============
    @Column(name = "dispatch_to")
    private String dispatchTo; // CUSTOMER or BANGALORE (auto-set based on repair_type)

    @Column(name = "is_dispatched")
    private Boolean isDispatched = false; // Simple boolean to track dispatch status

    @Column(name = "dispatched_date")
    private ZonedDateTime dispatchedDate; // When item was dispatched

    @Column(name = "dispatched_by_email")
    private String dispatchedByEmail;

    @Column(name = "dispatched_by_name")
    private String dispatchedByName;

    @Column(name = "rma_status")
    private String rmaStatus = "OPEN"; // OPEN, IN_PROGRESS, DISPATCHED, CLOSED

    // ============ DELIVERY CONFIRMATION ============
    @Column(name = "delivered_to")
    private String deliveredTo; // Name of person who received the item

    @Column(name = "delivered_by")
    private String deliveredBy; // Name of person/courier who delivered

    @Column(name = "delivery_date")
    private ZonedDateTime deliveryDate; // When it was delivered

    @Column(name = "delivery_confirmed_by_email")
    private String deliveryConfirmedByEmail; // Who recorded the delivery in system

    @Column(name = "delivery_confirmed_by_name")
    private String deliveryConfirmedByName;

    @Column(name = "delivery_confirmed_date")
    private ZonedDateTime deliveryConfirmedDate; // When delivery was recorded

    @Column(name = "delivery_notes", length = 1000)
    private String deliveryNotes; // POD number, signature notes, etc.

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
    @com.fasterxml.jackson.annotation.JsonBackReference
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
        // Auto-set dispatch destination based on repair type
        if ("LOCAL".equalsIgnoreCase(repairType)) {
            this.dispatchTo = "CUSTOMER";
        } else if ("DEPOT".equalsIgnoreCase(repairType)) {
            this.dispatchTo = "BANGALORE";
        }
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

    public String getLastReassignmentReason() {
        return lastReassignmentReason;
    }

    public void setLastReassignmentReason(String lastReassignmentReason) {
        this.lastReassignmentReason = lastReassignmentReason;
    }

    // ============ DEPOT RETURN GETTERS/SETTERS ============
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Boolean getDepotCycleClosed() {
        return depotCycleClosed;
    }

    public void setDepotCycleClosed(Boolean depotCycleClosed) {
        this.depotCycleClosed = depotCycleClosed;
    }

    public String getDepotReturnMethod() {
        return depotReturnMethod;
    }

    public void setDepotReturnMethod(String depotReturnMethod) {
        this.depotReturnMethod = depotReturnMethod;
    }

    public ZonedDateTime getDepotReturnDispatchDate() {
        return depotReturnDispatchDate;
    }

    public void setDepotReturnDispatchDate(ZonedDateTime depotReturnDispatchDate) {
        this.depotReturnDispatchDate = depotReturnDispatchDate;
    }

    public String getDepotReturnCourierName() {
        return depotReturnCourierName;
    }

    public void setDepotReturnCourierName(String depotReturnCourierName) {
        this.depotReturnCourierName = depotReturnCourierName;
    }

    public String getDepotReturnTrackingNo() {
        return depotReturnTrackingNo;
    }

    public void setDepotReturnTrackingNo(String depotReturnTrackingNo) {
        this.depotReturnTrackingNo = depotReturnTrackingNo;
    }

    public String getDepotReturnHandlerName() {
        return depotReturnHandlerName;
    }

    public void setDepotReturnHandlerName(String depotReturnHandlerName) {
        this.depotReturnHandlerName = depotReturnHandlerName;
    }

    public String getDepotReturnHandlerContact() {
        return depotReturnHandlerContact;
    }

    public void setDepotReturnHandlerContact(String depotReturnHandlerContact) {
        this.depotReturnHandlerContact = depotReturnHandlerContact;
    }

    public String getDepotProofOfDeliveryFileId() {
        return depotProofOfDeliveryFileId;
    }

    public void setDepotProofOfDeliveryFileId(String depotProofOfDeliveryFileId) {
        this.depotProofOfDeliveryFileId = depotProofOfDeliveryFileId;
    }

    public String getDepotProofOfDeliveryRemarks() {
        return depotProofOfDeliveryRemarks;
    }

    public void setDepotProofOfDeliveryRemarks(String depotProofOfDeliveryRemarks) {
        this.depotProofOfDeliveryRemarks = depotProofOfDeliveryRemarks;
    }

    public ZonedDateTime getDepotReturnDeliveredDate() {
        return depotReturnDeliveredDate;
    }

    public void setDepotReturnDeliveredDate(ZonedDateTime depotReturnDeliveredDate) {
        this.depotReturnDeliveredDate = depotReturnDeliveredDate;
    }

    // ============ DISPATCH TRACKING GETTERS/SETTERS ============
    public String getDispatchTo() {
        return dispatchTo;
    }

    public void setDispatchTo(String dispatchTo) {
        this.dispatchTo = dispatchTo;
    }

    public Boolean getIsDispatched() {
        return isDispatched;
    }

    public void setIsDispatched(Boolean isDispatched) {
        this.isDispatched = isDispatched;
    }

    public ZonedDateTime getDispatchedDate() {
        return dispatchedDate;
    }

    public void setDispatchedDate(ZonedDateTime dispatchedDate) {
        this.dispatchedDate = dispatchedDate;
    }

    public String getDispatchedByEmail() {
        return dispatchedByEmail;
    }

    public void setDispatchedByEmail(String dispatchedByEmail) {
        this.dispatchedByEmail = dispatchedByEmail;
    }

    public String getDispatchedByName() {
        return dispatchedByName;
    }

    public void setDispatchedByName(String dispatchedByName) {
        this.dispatchedByName = dispatchedByName;
    }

    public String getRmaStatus() {
        return rmaStatus;
    }

    public void setRmaStatus(String rmaStatus) {
        this.rmaStatus = rmaStatus;
    }

    // ============ DELIVERY CONFIRMATION GETTERS/SETTERS ============
    public String getDeliveredTo() {
        return deliveredTo;
    }

    public void setDeliveredTo(String deliveredTo) {
        this.deliveredTo = deliveredTo;
    }

    public String getDeliveredBy() {
        return deliveredBy;
    }

    public void setDeliveredBy(String deliveredBy) {
        this.deliveredBy = deliveredBy;
    }

    public ZonedDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(ZonedDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryConfirmedByEmail() {
        return deliveryConfirmedByEmail;
    }

    public void setDeliveryConfirmedByEmail(String deliveryConfirmedByEmail) {
        this.deliveryConfirmedByEmail = deliveryConfirmedByEmail;
    }

    public String getDeliveryConfirmedByName() {
        return deliveryConfirmedByName;
    }

    public void setDeliveryConfirmedByName(String deliveryConfirmedByName) {
        this.deliveryConfirmedByName = deliveryConfirmedByName;
    }

    public ZonedDateTime getDeliveryConfirmedDate() {
        return deliveryConfirmedDate;
    }

    public void setDeliveryConfirmedDate(ZonedDateTime deliveryConfirmedDate) {
        this.deliveryConfirmedDate = deliveryConfirmedDate;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    // Helper method to mark item as dispatched
    public void markAsDispatched(String byEmail, String byName) {
        this.isDispatched = true;
        this.dispatchedDate = ZonedDateTime.now();
        this.dispatchedByEmail = byEmail;
        this.dispatchedByName = byName;
        this.rmaStatus = "DISPATCHED";
    }

    // Helper method to confirm delivery and close RMA (for LOCAL items)
    public void confirmDelivery(String deliveredTo, String deliveredBy, String notes,
            String confirmedByEmail, String confirmedByName) {
        this.deliveredTo = deliveredTo;
        this.deliveredBy = deliveredBy;
        this.deliveryDate = ZonedDateTime.now();
        this.deliveryConfirmedByEmail = confirmedByEmail;
        this.deliveryConfirmedByName = confirmedByName;
        this.deliveryConfirmedDate = ZonedDateTime.now();
        this.deliveryNotes = notes;

        // Auto-close RMA for LOCAL repairs after delivery
        if ("LOCAL".equalsIgnoreCase(this.repairType)) {
            this.rmaStatus = "CLOSED";
            this.localStage = "DELIVERED";
        }
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
