package com.serverManagement.server.management.entity.rma;

import java.time.ZonedDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Table(name = "rma_request")
@Entity
public class RmaRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Form Information
    private String dplLicense;
    private String date;
    private String modeOfTransport;
    private String shippingMethod;
    private String courierCompanyName;

    // Return Address Details (kept for backward compatibility)
    @Column(nullable = false)
    private String companyName;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String contactName;
    @Column(nullable = false)
    private String telephone;
    @Column(nullable = false)
    private String mobile;
    @Column(nullable = false, length = 1000)
    private String returnAddress;

    // Invoice Address Details (optional, kept for backward compatibility)
    private String invoiceCompanyName;
    private String invoiceEmail;
    private String invoiceContactName;
    private String invoiceTelephone;
    private String invoiceMobile;
    @Column(length = 1000)
    private String invoiceAddress;

    // Signature
    private String signature;

    // Repair Type (Local Repair / Depot Repair)
    @Column(name = "repair_type")
    private String repairType;

    // Auto-generated Request Number (created when request is submitted)
    @Column(name = "request_number", unique = true, nullable = true)
    private String requestNumber;

    // RMA Number (assigned manually by service team after approval)
    // This number is shared by all items in the request once approved
    @Column(name = "rma_no", unique = true, nullable = true)
    private String rmaNo;

    // Audit Fields
    private String createdByEmail;
    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;

    // Relationship with RMA Items
    @OneToMany(mappedBy = "rmaRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RmaItemEntity> items;

    // Customer relationship (for return address customer)
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    // Invoice customer relationship (optional)
    @ManyToOne
    @JoinColumn(name = "invoice_customer_id")
    private CustomerEntity invoiceCustomer;

    // Constructors
    public RmaRequestEntity() {
        super();
    }

    public RmaRequestEntity(Long id, String dplLicense, String date, String modeOfTransport, String shippingMethod,
            String courierCompanyName, String companyName, String email, String contactName,
            String telephone, String mobile, String returnAddress, String invoiceCompanyName,
            String invoiceEmail, String invoiceContactName, String invoiceTelephone,
            String invoiceMobile, String invoiceAddress, String signature, String rmaNo,
            String createdByEmail, ZonedDateTime createdDate, ZonedDateTime updatedDate,
            List<RmaItemEntity> items) {
        super();
        this.id = id;
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
        this.rmaNo = rmaNo;
        this.createdByEmail = createdByEmail;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.items = items;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDplLicense() {
        return dplLicense;
    }

    public void setDplLicense(String dplLicense) {
        this.dplLicense = dplLicense;
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

    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }

    public String getRmaNo() {
        return rmaNo;
    }

    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public ZonedDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(ZonedDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public List<RmaItemEntity> getItems() {
        return items;
    }

    public void setItems(List<RmaItemEntity> items) {
        this.items = items;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public CustomerEntity getInvoiceCustomer() {
        return invoiceCustomer;
    }

    public void setInvoiceCustomer(CustomerEntity invoiceCustomer) {
        this.invoiceCustomer = invoiceCustomer;
    }

    @Override
    public String toString() {
        return "RmaRequestEntity [id=" + id + ", dplLicense=" + dplLicense + ", date=" + date + ", modeOfTransport="
                + modeOfTransport + ", shippingMethod=" + shippingMethod + ", courierCompanyName=" + courierCompanyName
                + ", companyName=" + companyName + ", email=" + email + ", contactName=" + contactName + ", telephone="
                + telephone + ", mobile=" + mobile + ", returnAddress=" + returnAddress + ", invoiceCompanyName="
                + invoiceCompanyName + ", invoiceEmail=" + invoiceEmail + ", invoiceContactName=" + invoiceContactName
                + ", invoiceTelephone=" + invoiceTelephone + ", invoiceMobile=" + invoiceMobile + ", invoiceAddress="
                + invoiceAddress + ", signature=" + signature + ", rmaNo=" + rmaNo + ", createdByEmail="
                + createdByEmail + ", createdDate=" + createdDate + ", updatedDate=" + updatedDate + "]";
    }
}
