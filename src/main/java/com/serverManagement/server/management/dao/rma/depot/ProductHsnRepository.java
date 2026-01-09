package com.serverManagement.server.management.dao.rma.depot;

import com.serverManagement.server.management.entity.rma.depot.ProductHsnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductHsnRepository extends JpaRepository<ProductHsnEntity, Long> {
    Optional<ProductHsnEntity> findByProductName(String productName);
}
