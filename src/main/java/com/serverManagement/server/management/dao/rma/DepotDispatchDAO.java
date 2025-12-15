package com.serverManagement.server.management.dao.rma;

import com.serverManagement.server.management.entity.rma.DepotDispatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepotDispatchDAO extends JpaRepository<DepotDispatchEntity, Long> {
    // Add custom queries if needed
}
