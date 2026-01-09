package com.serverManagement.server.management.entity.rma.common;

import jakarta.persistence.*;

@Entity
@Table(name = "transporter")
public class TransporterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "transporter_id", nullable = true)
    private String transporterId; // Business ID like "27AAACB0446L1ZS"

    @Column(name = "gst_in")
    private String gstIn;

    public TransporterEntity() {
    }

    public TransporterEntity(String name, String transporterId) {
        this.name = name;
        this.transporterId = transporterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(String transporterId) {
        this.transporterId = transporterId;
    }

    public String getGstIn() {
        return gstIn;
    }

    public void setGstIn(String gstIn) {
        this.gstIn = gstIn;
    }
}
