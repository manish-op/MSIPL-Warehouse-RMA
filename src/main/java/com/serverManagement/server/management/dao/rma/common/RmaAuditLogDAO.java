package com.serverManagement.server.management.dao.rma.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.entity.rma.common.RmaAuditLogEntity;

/**
 * Repository for RMA audit log operations
 */
@Repository
public interface RmaAuditLogDAO extends JpaRepository<RmaAuditLogEntity, Long> {

    // Find all audit logs for a specific RMA item
    @Query("SELECT a FROM RmaAuditLogEntity a WHERE a.rmaItemId = :itemId ORDER BY a.performedAt DESC")
    List<RmaAuditLogEntity> findByRmaItemId(@Param("itemId") Long itemId);

    // Find all audit logs for a specific RMA number
    @Query("SELECT a FROM RmaAuditLogEntity a WHERE a.rmaNo = :rmaNo ORDER BY a.performedAt DESC")
    List<RmaAuditLogEntity> findByRmaNo(@Param("rmaNo") String rmaNo);

    // Find audit logs by action type
    @Query("SELECT a FROM RmaAuditLogEntity a WHERE a.action = :action ORDER BY a.performedAt DESC")
    List<RmaAuditLogEntity> findByAction(@Param("action") String action);

    // Find audit logs by user
    @Query("SELECT a FROM RmaAuditLogEntity a WHERE a.performedByEmail = :email ORDER BY a.performedAt DESC")
    List<RmaAuditLogEntity> findByPerformedByEmail(@Param("email") String email);

    // Get all audit logs ordered by date (most recent first)
    @Query("SELECT a FROM RmaAuditLogEntity a ORDER BY a.performedAt DESC")
    List<RmaAuditLogEntity> findAllOrderByPerformedAtDesc();
}
