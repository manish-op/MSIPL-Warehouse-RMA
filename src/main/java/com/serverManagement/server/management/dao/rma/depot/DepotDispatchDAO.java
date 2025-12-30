package com.serverManagement.server.management.dao.rma.depot;

import com.serverManagement.server.management.entity.rma.depot.DepotDispatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepotDispatchDAO extends JpaRepository<DepotDispatchEntity, Long> {
    DepotDispatchEntity findTopByOrderByIdDesc();

    // Find latest dispatch that actually has a DC number, ordered by DATE (handle
    // updates to old records)
    DepotDispatchEntity findTopByDcNoIsNotNullOrderByDispatchDateDesc();
}
