package com.serverManagement.server.management.entity.rma;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "rma_product_values", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "product", "model" })
})
public class ProductValueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private String model;

    @Column(name = "rate_value")
    private String value;

    @Column(name = "last_updated")
    private ZonedDateTime lastUpdated;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(ZonedDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
