package com.serverManagement.server.management.dto.rma.common;

public class ProductModelDTO {
    private String product;
    private String model;

    public ProductModelDTO() {
    }

    public ProductModelDTO(String product, String model) {
        this.product = product;
        this.model = model;
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
}
