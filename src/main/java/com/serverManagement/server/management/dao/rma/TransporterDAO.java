package com.serverManagement.server.management.dao.rma;

import com.serverManagement.server.management.entity.Transporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransporterDAO extends JpaRepository<Transporter, Long> {
    Optional<Transporter> findByName(String name);
}
