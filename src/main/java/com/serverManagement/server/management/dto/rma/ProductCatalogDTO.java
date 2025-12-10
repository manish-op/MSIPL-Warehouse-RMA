package com.serverManagement.server.management.dto.rma;

/**
 * DTO for product catalog items - used in RMA form dropdown
 */
public class ProductCatalogDTO {
    private String name;
    private String model;
    private String partNo;

    public ProductCatalogDTO() {
    }

    public ProductCatalogDTO(String name, String model, String partNo) {
        this.name = name;
        this.model = model;
        this.partNo = partNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }
}
