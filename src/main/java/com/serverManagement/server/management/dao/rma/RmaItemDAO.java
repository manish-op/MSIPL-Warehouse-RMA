package com.serverManagement.server.management.dao.rma;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.dto.rma.DepotDispatchItemDto;
import com.serverManagement.server.management.entity.rma.RmaItemEntity;

@Repository
public interface RmaItemDAO extends JpaRepository<RmaItemEntity, Long> {

    // Count items by repair status (case-insensitive)
    @Query("SELECT COUNT(r) FROM RmaItemEntity r WHERE LOWER(r.repairStatus) = LOWER(:status)")
    long countByRepairStatusIgnoreCase(@Param("status") String status);

    // Count items where repair status contains a keyword (for flexible matching)
    @Query("SELECT COUNT(r) FROM RmaItemEntity r WHERE LOWER(r.repairStatus) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    long countByRepairStatusContaining(@Param("keyword") String keyword);

    // Count items that are NOT repaired (null, empty, or not containing 'repaired')
    @Query("SELECT COUNT(r) FROM RmaItemEntity r WHERE r.repairStatus IS NULL OR r.repairStatus = '' OR LOWER(r.repairStatus) NOT LIKE '%repaired%'")
    long countUnrepaired();

    // Count items that ARE repaired
    @Query("SELECT COUNT(r) FROM RmaItemEntity r WHERE LOWER(r.repairStatus) = 'repaired'")
    long countRepaired();

    // ============ WORKFLOW QUERIES ============

    // Find unassigned items (no assignee yet)
    // Local: all unassigned
    // Depot: only those received at depot (AT_DEPOT_UNREPAIRED)
    @Query("""
            SELECT r FROM RmaItemEntity r
            WHERE (r.assignedToEmail IS NULL OR r.assignedToEmail = '')
                AND (
                    r.repairType IS NULL
                    OR r.repairType <> 'DEPOT'
                    OR r.depotStage = 'AT_DEPOT_UNREPAIRED'
                )
            """)
    List<RmaItemEntity> findUnassignedItems();

    // Find assigned items (has assignee, not yet repaired or cant be repaired or
    // BER)
    @Query("SELECT r FROM RmaItemEntity r WHERE r.assignedToEmail IS NOT NULL AND r.assignedToEmail != '' " +
            "AND (r.repairStatus IS NULL OR (LOWER(r.repairStatus) != 'repaired' AND LOWER(r.repairStatus) != 'cant_be_repaired' AND LOWER(r.repairStatus) != 'ber'))")
    List<RmaItemEntity> findAssignedItems();

    // Find repaired items
    @Query("SELECT r FROM RmaItemEntity r WHERE LOWER(r.repairStatus) = 'repaired'")
    List<RmaItemEntity> findRepairedItems();

    // Find items that can't be repaired (includes CANT_BE_REPAIRED and BER)
    @Query("SELECT r FROM RmaItemEntity r WHERE LOWER(r.repairStatus) = 'cant_be_repaired' OR LOWER(r.repairStatus) = 'ber'")
    List<RmaItemEntity> findCantBeRepairedItems();

    // Find items assigned to a specific user
    @Query("SELECT r FROM RmaItemEntity r WHERE r.assignedToEmail = :email")
    List<RmaItemEntity> findByAssignedToEmail(@Param("email") String email);

    // Find items by their item-level RMA number (legacy)
    List<RmaItemEntity> findByRmaNo(String rmaNo);

    // Find unassigned items by RMA number (for bulk assignment)
    // Checks both Parent Request Number AND Item-level RMA Number (legacy/manual)
    @Query("SELECT r FROM RmaItemEntity r WHERE (r.rmaRequest.requestNumber = :rmaNo OR r.rmaNo = :rmaNo) AND (r.assignedToEmail IS NULL OR r.assignedToEmail = '')")
    List<RmaItemEntity> findUnassignedByRmaNo(@Param("rmaNo") String rmaNo);

    // -----------New Methods for Depot Dispatch------------------
    // Depot: items waiting for dispatch to bangalore
    List<RmaItemEntity> findByRepairTypeAndDepotStage(String repairType, String depotStage);

    // Depot: items in specific stages (e.g., In Transit, Received, etc.)
    List<RmaItemEntity> findByRepairTypeAndDepotStageIn(String repairType, List<String> depotStages);

    // Explicit
    List<RmaItemEntity> findAllById(Iterable<Long> ids);

<<<<<<< HEAD
    List<RmaItemEntity> findByRmaRequest(com.serverManagement.server.management.entity.rma.RmaRequestEntity rmaRequest);
=======
    Collection<DepotDispatchItemDto> findByRepairTypeAndDepotStage(String string, String string2);

>>>>>>> origin/priyanshi
}
