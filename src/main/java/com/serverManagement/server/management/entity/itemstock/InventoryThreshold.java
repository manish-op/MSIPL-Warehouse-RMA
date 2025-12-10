package com.serverManagement.server.management.entity.itemstock;
import com.serverManagement.server.management.entity.region.RegionEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "inventory_thresholds")
public class InventoryThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String partNo; // The PartNo we are tracking

    @Column(nullable = false)
    private Integer minQuantity; // The minimum allowed quantity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region; // The region this rule applies to

    // No-argument constructor (required by JPA)
    public InventoryThreshold() {
        super();
    }

    // Constructor for convenience
    public InventoryThreshold(String partNo, Integer minQuantity, RegionEntity region) {
        super();
        this.partNo = partNo;
        this.minQuantity = minQuantity;
        this.region = region;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public RegionEntity getRegion() {
        return region;
    }

    public void setRegion(RegionEntity region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "InventoryThreshold [id=" + id + ", partNo=" + partNo +
                ", minQuantity=" + minQuantity + ", regionId=" + (region != null ? region.getId() : "null") + "]";
    }
}