package com.serverManagement.server.management.dao.rma.common;

import com.serverManagement.server.management.entity.rma.common.TransporterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransporterDAO extends JpaRepository<TransporterEntity, Long> {
    Optional<TransporterEntity> findByName(String name);

    boolean existsByName(String name);
}
