package com.serverManagement.server.management.dao.rma.common;

import com.serverManagement.server.management.entity.rma.common.ProductValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductValueDAO extends JpaRepository<ProductValueEntity, Long> {
    Optional<ProductValueEntity> findByProductAndModel(String product, String model);
}
