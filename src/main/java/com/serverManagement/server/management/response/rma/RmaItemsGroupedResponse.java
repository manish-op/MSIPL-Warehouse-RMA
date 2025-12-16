package com.serverManagement.server.management.response.rma;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Response DTO for RMA items grouped by RMA request number
 */
public class RmaItemsGroupedResponse {

    private String rmaNo;
    private String companyName;
    private String contactName;
    private ZonedDateTime createdDate;
    private List<RmaItemDTO> items;

    // Inner class for simplified item data
    public static class RmaItemDTO {
        private Long id;
        private String product;
        private String serialNo;
        private String model;
        private String faultDescription;
        private String repairStatus;

        public RmaItemDTO() {
        }

        public RmaItemDTO(Long id, String product, String serialNo, String model,
                String faultDescription, String repairStatus) {
            this.id = id;
            this.product = product;
            this.serialNo = serialNo;
            this.model = model;
            this.faultDescription = faultDescription;
            this.repairStatus = repairStatus;
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

        public String getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(String serialNo) {
            this.serialNo = serialNo;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getFaultDescription() {
            return faultDescription;
        }

        public void setFaultDescription(String faultDescription) {
            this.faultDescription = faultDescription;
        }

        public String getRepairStatus() {
            return repairStatus;
        }

        public void setRepairStatus(String repairStatus) {
            this.repairStatus = repairStatus;
        }
    }

    // Constructors
    public RmaItemsGroupedResponse() {
    }

    public RmaItemsGroupedResponse(String rmaNo, String companyName, String contactName,
            ZonedDateTime createdDate, List<RmaItemDTO> items) {
        this.rmaNo = rmaNo;
        this.companyName = companyName;
        this.contactName = contactName;
        this.createdDate = createdDate;
        this.items = items;
    }

    // Getters and Setters
    public String getRmaNo() {
        return rmaNo;
    }

    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public List<RmaItemDTO> getItems() {
        return items;
    }

    public void setItems(List<RmaItemDTO> items) {
        this.items = items;
    }
}
