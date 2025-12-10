package com.serverManagement.server.management.request.rma;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class RmaCreateRequest {

    public String subject;
    public String status;
    public String notes;

    public AddressDto returnAddress;
    public AddressDto invoiceAddress;
    public CourierDto courierDetails;

    @NotNull
    @NotEmpty
    public List<RmaItemDto> items;

    public static class AddressDto {
        public String attention;
        public String companyName;
        public String addressLine1;
        public String addressLine2;
        public String city;
        public String state;
        public String postalCode;
        public String country;
        public String phone;
        public String email;
        public String addressType;
    }

    public static class CourierDto {
        public String courierName;
        public String awbNumber;
        public LocalDate pickupDate;       // ISO date yyyy-MM-dd
        public LocalDate expectedDelivery; // ISO date yyyy-MM-dd
        public String contactNumber;
        public String notes;
    }

    public static class RmaItemDto {
        public Integer itemNo; // optional — service will reassign sequentially
        public String product;
        public String modelOrPartNo;
        public String serialNumber;
        public String rmaNumber; // optional in request, service will set
        public String faultDescription;
        public String codeplugProgramming;
        public String flashCode;
        public String status; // WARR/OOW/AMC/SFS, etc
        public String invoiceNumber;
        public String dateCode;
        public Boolean fmUlAtexMandatory; // true/false
        public String encryption;
        public String firmwareVersion;
        public String lowerFirmwareVersion;
        public String remarks;
        public Integer quantity;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AddressDto getReturnAddress() {
        return returnAddress;
    }

    public void setReturnAddress(AddressDto returnAddress) {
        this.returnAddress = returnAddress;
    }

    public AddressDto getInvoiceAddress() {
        return invoiceAddress;
    }

    public void setInvoiceAddress(AddressDto invoiceAddress) {
        this.invoiceAddress = invoiceAddress;
    }

    public CourierDto getCourierDetails() {
        return courierDetails;
    }

    public void setCourierDetails(CourierDto courierDetails) {
        this.courierDetails = courierDetails;
    }

    public List<RmaItemDto> getItems() {
        return items;
    }

    public void setItems(List<RmaItemDto> items) {
        this.items = items;
    }
}
