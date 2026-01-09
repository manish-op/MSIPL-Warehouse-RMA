package com.serverManagement.server.management.dao.rma.depot;

import com.serverManagement.server.management.entity.rma.depot.SavedAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedAddressRepository extends JpaRepository<SavedAddressEntity, Long> {
    Optional<SavedAddressEntity> findByName(String name);
}
