package com.serverManagement.server.management.dao.rma;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.entity.rma.RmaInwardGatepassEntity;

@Repository
public interface RmaInwardGatepassDAO extends JpaRepository<RmaInwardGatepassEntity, Long> {

    // Find by gatepass number
    Optional<RmaInwardGatepassEntity> findByGatepassNumber(String gatepassNumber);

    // Find by RMA request ID
    List<RmaInwardGatepassEntity> findByRmaRequestId(Long rmaRequestId);

    // Get the latest gatepass number for sequence generation
    @Query("SELECT MAX(g.id) FROM RmaInwardGatepassEntity g")
    Long findMaxId();

    // Count gatepasses for a specific RMA request
    long countByRmaRequestId(Long rmaRequestId);
}
