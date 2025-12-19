package com.serverManagement.server.management.dao.rma;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.entity.rma.RmaItemEntity;
import com.serverManagement.server.management.entity.rma.RmaRequestEntity;

@Repository
public interface RmaItemDAO extends JpaRepository<RmaItemEntity, Long> {

    // Find all items belonging to a specific RMA request
    List<RmaItemEntity> findByRmaRequest(RmaRequestEntity rmaRequest);

    // Find items by parent Request Number
    List<RmaItemEntity> findByRmaRequest_RequestNumber(String requestNumber);

    // Count items by repair status (case-insensitive)
    @Query("SELECT COUNT(r) FROM RmaItemEntity r WHERE LOWER(r.repairStatus) = LOWER(:status)")
    long countByRepairStatusIgnoreCase(@Param("status") String status);

    // Count items where repair status contains a keyword (for flexible matching)
    @Query("SELECT COUNT(r) FROM RmaItemEntity r WHERE LOWER(r.repairStatus) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    long countByRepairStatusContaining(@Param("keyword") String keyword);

    // Count items that are NOT repaired AND NOT replaced
    @Query("SELECT COUNT(r) FROM RmaItemEntity r WHERE (r.repairStatus IS NULL OR r.repairStatus = '') OR (LOWER(r.repairStatus) NOT LIKE '%repaired%' AND LOWER(r.repairStatus) NOT LIKE '%replaced%' AND LOWER(r.repairStatus) NOT LIKE '%dispatched%' AND LOWER(r.repairStatus) NOT LIKE '%received_at_gurgaon%' AND LOWER(r.repairStatus) NOT LIKE '%delivered%' AND LOWER(r.repairStatus) != 'closed')")
    long countUnrepaired();

    // Count items that ARE repaired OR replaced
    @Query("SELECT COUNT(r) FROM RmaItemEntity r WHERE LOWER(r.repairStatus) IN ('repaired', 'replaced', 'repaired_at_depot', 'received_at_gurgaon', 'dispatched_to_depot', 'received_at_depot', 'dispatched_to_customer', 'delivered_to_customer', 'closed')")
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

    // Find assigned items (has assignee, not yet repaired/replaced or cant be
    // repaired or BER)
    @Query("SELECT r FROM RmaItemEntity r WHERE r.assignedToEmail IS NOT NULL AND r.assignedToEmail != '' " +
            "AND (r.repairStatus IS NULL OR (LOWER(r.repairStatus) != 'repaired' AND LOWER(r.repairStatus) != 'replaced' AND LOWER(r.repairStatus) != 'cant_be_repaired' AND LOWER(r.repairStatus) != 'ber'))")
    List<RmaItemEntity> findAssignedItems();

    // Find repaired OR replaced items (both ready for dispatch)
    @Query("SELECT r FROM RmaItemEntity r WHERE LOWER(r.repairStatus) IN ('repaired', 'replaced', 'repaired_at_depot', 'received_at_gurgaon', 'dispatched_to_depot', 'received_at_depot', 'dispatched_to_customer', 'delivered_to_customer', 'closed')")
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
    // Note: Uses requestNumber (auto-generated) since rmaNo is assigned later
    @Query("SELECT r FROM RmaItemEntity r WHERE r.rmaRequest.requestNumber = :rmaNo AND (r.assignedToEmail IS NULL OR r.assignedToEmail = '')")
    List<RmaItemEntity> findUnassignedByRmaNo(@Param("rmaNo") String rmaNo);

    // -----------New Methods for Depot Dispatch------------------
    // Depot: items waiting for dispatch to bangalore
    List<RmaItemEntity> findByRepairTypeAndDepotStage(String repairType, String depotStage);

    // Depot: items in specific stages (e.g., In Transit, Received, etc.)
    List<RmaItemEntity> findByRepairTypeAndDepotStageIn(String repairType, List<String> depotStages);

    // Explicit
    List<RmaItemEntity> findAllById(Iterable<Long> ids);

    // -----------New Methods for Dispatch Tracking------------------
    // Find items by dispatch status
    List<RmaItemEntity> findByIsDispatched(Boolean isDispatched);

    // Find items by RMA status
    List<RmaItemEntity> findByRmaStatus(String rmaStatus);

    // Find items by dispatch destination
    List<RmaItemEntity> findByDispatchTo(String dispatchTo);

    // Find items pending dispatch (repaired OR replaced but not yet dispatched)
    @Query("SELECT r FROM RmaItemEntity r WHERE (LOWER(r.repairStatus) = 'repaired' OR LOWER(r.repairStatus) = 'replaced') AND (r.isDispatched IS NULL OR r.isDispatched = false)")
    List<RmaItemEntity> findRepairedPendingDispatch();

    // Find dispatched items pending delivery confirmation
    @Query("SELECT r FROM RmaItemEntity r WHERE r.isDispatched = true AND r.deliveryConfirmedDate IS NULL")
    List<RmaItemEntity> findDispatchedPendingDelivery();

    // Search items by Product or Serial Number (case-insensitive)
    @Query("SELECT r FROM RmaItemEntity r WHERE LOWER(r.product) LIKE LOWER(:query) OR LOWER(r.serialNo) LIKE LOWER(:query)")
    List<RmaItemEntity> searchItems(@Param("query") String query);

}