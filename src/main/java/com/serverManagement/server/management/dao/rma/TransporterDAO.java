package com.serverManagement.server.management.dao.rma;

<<<<<<< HEAD
import com.serverManagement.server.management.entity.rma.TransporterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransporterDAO extends JpaRepository<TransporterEntity, Long> {
    Optional<TransporterEntity> findByName(String name);

    boolean existsByName(String name);
=======
import com.serverManagement.server.management.entity.Transporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransporterDAO extends JpaRepository<Transporter, Long> {
    Optional<Transporter> findByName(String name);
>>>>>>> origin/priyanshi
}
