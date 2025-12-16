package com.serverManagement.server.management.dao.rma;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.entity.rma.RmaOutwardGatepassEntity;

@Repository
public interface RmaOutwardGatepassDAO extends JpaRepository<RmaOutwardGatepassEntity, Long> {

    // Find by gatepass number
    Optional<RmaOutwardGatepassEntity> findByGatepassNumber(String gatepassNumber);

    // Find by RMA request ID
    List<RmaOutwardGatepassEntity> findByRmaRequestId(Long rmaRequestId);

    // Get the latest gatepass number for sequence generation
    @Query("SELECT MAX(g.id) FROM RmaOutwardGatepassEntity g")
    Long findMaxId();

    // Count gatepasses for a specific RMA request
    long countByRmaRequestId(Long rmaRequestId);
}
