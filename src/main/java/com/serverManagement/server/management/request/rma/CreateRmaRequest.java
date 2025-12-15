package com.serverManagement.server.management.request.rma;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRmaRequest {

    // Basic Form Information
    private String dplLicense;
    private String date;
    private String modeOfTransport;
    private String shippingMethod;
    private String courierCompanyName;

    // Return Address Details
    private String companyName;
    private String email;
    private String contactName;
    private String telephone;
    private String mobile;
    private String returnAddress;

    // Invoice Address Details (optional)
    private String invoiceCompanyName;
    private String invoiceEmail;
    private String invoiceContactName;
    private String invoiceTelephone;
    private String invoiceMobile;
    private String invoiceAddress;

    // Signature
    private String signature;
    private String repairType;

    // List of Items
    private List<RmaItemRequest> items;

    // Constructors
    public CreateRmaRequest() {
        super();
    }

    public CreateRmaRequest(String dplLicense, String date, String modeOfTransport, String shippingMethod,
            String courierCompanyName, String companyName, String email, String contactName,
            String telephone, String mobile, String returnAddress, String invoiceCompanyName,
            String invoiceEmail, String invoiceContactName, String invoiceTelephone,
            String invoiceMobile, String invoiceAddress, String signature, String repairType,
            List<RmaItemRequest> items) {
        super();
        this.dplLicense = dplLicense;
        this.date = date;
        this.modeOfTransport = modeOfTransport;
        this.shippingMethod = shippingMethod;
        this.courierCompanyName = courierCompanyName;
        this.companyName = companyName;
        this.email = email;
        this.contactName = contactName;
        this.telephone = telephone;
        this.mobile = mobile;
        this.returnAddress = returnAddress;
        this.invoiceCompanyName = invoiceCompanyName;
        this.invoiceEmail = invoiceEmail;
        this.invoiceContactName = invoiceContactName;
        this.invoiceTelephone = invoiceTelephone;
        this.invoiceMobile = invoiceMobile;
        this.invoiceAddress = invoiceAddress;
        this.signature = signature;
        this.repairType = repairType;
        this.items = items;
    }

    // Getters and Setters
    public String getDplLicense() {
        return dplLicense;
    }

    public void setDplLicense(String dplLicense) {
        this.dplLicense = dplLicense;
    }

    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getModeOfTransport() {
        return modeOfTransport;
    }

    public void setModeOfTransport(String modeOfTransport) {
        this.modeOfTransport = modeOfTransport;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getCourierCompanyName() {
        return courierCompanyName;
    }

    public void setCourierCompanyName(String courierCompanyName) {
        this.courierCompanyName = courierCompanyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getReturnAddress() {
        return returnAddress;
    }

    public void setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
    }

    public String getInvoiceCompanyName() {
        return invoiceCompanyName;
    }

    public void setInvoiceCompanyName(String invoiceCompanyName) {
        this.invoiceCompanyName = invoiceCompanyName;
    }

    public String getInvoiceEmail() {
        return invoiceEmail;
    }

    public void setInvoiceEmail(String invoiceEmail) {
        this.invoiceEmail = invoiceEmail;
    }

    public String getInvoiceContactName() {
        return invoiceContactName;
    }

    public void setInvoiceContactName(String invoiceContactName) {
        this.invoiceContactName = invoiceContactName;
    }

    public String getInvoiceTelephone() {
        return invoiceTelephone;
    }

    public void setInvoiceTelephone(String invoiceTelephone) {
        this.invoiceTelephone = invoiceTelephone;
    }

    public String getInvoiceMobile() {
        return invoiceMobile;
    }

    public void setInvoiceMobile(String invoiceMobile) {
        this.invoiceMobile = invoiceMobile;
    }

    public String getInvoiceAddress() {
        return invoiceAddress;
    }

    public void setInvoiceAddress(String invoiceAddress) {
        this.invoiceAddress = invoiceAddress;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<RmaItemRequest> getItems() {
        return items;
    }

    public void setItems(List<RmaItemRequest> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "CreateRmaRequest [dplLicense=" + dplLicense + ", date=" + date + ", modeOfTransport=" + modeOfTransport
                + ", shippingMethod=" + shippingMethod + ", courierCompanyName=" + courierCompanyName + ", companyName="
                + companyName + ", email=" + email + ", contactName=" + contactName + ", telephone=" + telephone
                + ", mobile=" + mobile + ", returnAddress=" + returnAddress + ", invoiceCompanyName="
                + invoiceCompanyName + ", invoiceEmail=" + invoiceEmail + ", invoiceContactName=" + invoiceContactName
                + ", invoiceTelephone=" + invoiceTelephone + ", invoiceMobile=" + invoiceMobile + ", invoiceAddress="
                + invoiceAddress + ", signature=" + signature + ", items=" + items + "]";
    }
}
