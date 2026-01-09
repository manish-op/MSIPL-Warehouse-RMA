package com.serverManagement.server.management.dao.rma.depot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.entity.rma.depot.DepotProofOfDeliveryEntity;

@Repository
public interface DepotProofOfDeliveryDAO extends JpaRepository<DepotProofOfDeliveryEntity, Long> {
}
