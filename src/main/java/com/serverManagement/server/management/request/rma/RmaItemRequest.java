package com.serverManagement.server.management.request.rma;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RmaItemRequest {

    private String product;
    private String model; // Model No./Part No.
    private String serialNo;
    private String faultDescription;
    private String codeplug;
    private String flashCode;
    private String repairStatus;

    private String invoiceNo;
    private String dateCode;
    private String fmUlatex;
    private String encryption;
    private String firmwareVersion;
    private String lowerFirmwareVersion;

    @com.fasterxml.jackson.annotation.JsonProperty("partialshipment")
    private String partialShipment;

    private String remarks;

    // Constructors
    public RmaItemRequest() {
        super();
    }

    public RmaItemRequest(String product, String model, String serialNo, String faultDescription, String codeplug,
            String flashCode, String repairStatus, String invoiceNo, String dateCode, String fmUlatex,
            String encryption, String firmwareVersion, String lowerFirmwareVersion, String remarks) {
        super();
        this.product = product;
        this.model = model;
        this.serialNo = serialNo;
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
    }

    // Getters and Setters
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

    public String getPartialShipment() {
        return partialShipment;
    }

    public void setPartialShipment(String partialShipment) {
        this.partialShipment = partialShipment;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "RmaItemRequest [product=" + product + ", model=" + model + ", serialNo=" + serialNo
                + ", faultDescription=" + faultDescription + ", codeplug=" + codeplug + ", flashCode=" + flashCode
                + ", repairStatus=" + repairStatus + ", invoiceNo=" + invoiceNo + ", dateCode=" + dateCode
                + ", fmUlatex="
                + fmUlatex + ", encryption=" + encryption + ", firmwareVersion=" + firmwareVersion
                + ", lowerFirmwareVersion=" + lowerFirmwareVersion + ", remarks=" + remarks + "]";
    }
}
