package com.serverManagement.server.management.dao.itemstock;

import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.itemstock.InventoryThreshold;
import com.serverManagement.server.management.entity.region.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryThresholdRepository extends JpaRepository<InventoryThreshold, Long> {

    Optional<InventoryThreshold> findByPartNoAndRegion(String partNo, RegionEntity region);

    List<InventoryThreshold> findByRegion(RegionEntity region);

}
