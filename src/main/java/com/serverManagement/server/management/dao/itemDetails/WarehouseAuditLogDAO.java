package com.serverManagement.server.management.dao.itemDetails;

import com.serverManagement.server.management.entity.itemDetails.WarehouseAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for warehouse audit log operations
 */
@Repository
public interface WarehouseAuditLogDAO extends JpaRepository<WarehouseAuditLogEntity, Long> {

    // Find all audit logs for a specific item
    @Query("SELECT a FROM WarehouseAuditLogEntity a WHERE a.itemId = :itemId ORDER BY a.performedAt DESC")
    List<WarehouseAuditLogEntity> findByItemId(@Param("itemId") Long itemId);

    // Find all audit logs for a specific serial number
    @Query("SELECT a FROM WarehouseAuditLogEntity a WHERE a.serialNo = :serialNo ORDER BY a.performedAt DESC")
    List<WarehouseAuditLogEntity> findBySerialNo(@Param("serialNo") String serialNo);

    // Find audit logs by action type
    @Query("SELECT a FROM WarehouseAuditLogEntity a WHERE a.action = :action ORDER BY a.performedAt DESC")
    List<WarehouseAuditLogEntity> findByAction(@Param("action") String action);

    // Find audit logs by user
    @Query("SELECT a FROM WarehouseAuditLogEntity a WHERE a.performedByEmail = :email ORDER BY a.performedAt DESC")
    List<WarehouseAuditLogEntity> findByPerformedByEmail(@Param("email") String email);

    // Find audit logs by region
    @Query("SELECT a FROM WarehouseAuditLogEntity a WHERE a.region = :region ORDER BY a.performedAt DESC")
    List<WarehouseAuditLogEntity> findByRegion(@Param("region") String region);

    // Get all audit logs ordered by date (most recent first)
    @Query("SELECT a FROM WarehouseAuditLogEntity a ORDER BY a.performedAt DESC")
    List<WarehouseAuditLogEntity> findAllOrderByPerformedAtDesc();
}
