package com.serverManagement.server.management.dao.itemDetails;

import java.util.List;

import com.serverManagement.server.management.entity.itemDetails.ItemHistoryUpdatedByAdminEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemHistoryUpdatedByAdminDAO extends JpaRepository<ItemHistoryUpdatedByAdminEntity, Long> {

    @Query("SELECT um FROM ItemHistoryUpdatedByAdminEntity um WHERE um.serial_No = :serial")
    List<ItemHistoryUpdatedByAdminEntity> getHistoryDetailsBySerialNo(@Param("serial") String serial);

    @Query("SELECT h FROM ItemHistoryUpdatedByAdminEntity h " + "WHERE (:city IS NULL OR lower(h.region.city) = :city) " + "ORDER BY h.update_Date DESC")
    List<ItemHistoryUpdatedByAdminEntity> findRecentByRegion(@Param("city") String city, Pageable pageable);

    @Query("SELECT h FROM ItemHistoryUpdatedByAdminEntity h ORDER BY h.update_Date DESC")
    List<ItemHistoryUpdatedByAdminEntity> findRecentAll(Pageable pageable);
}
