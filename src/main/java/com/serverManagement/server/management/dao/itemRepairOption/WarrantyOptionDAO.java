package com.serverManagement.server.management.dao.itemRepairOption;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serverManagement.server.management.entity.itemRepairOption.WarrantyOptionEntity;

public interface WarrantyOptionDAO extends JpaRepository<WarrantyOptionEntity, Long> {

	@Query("SELECT um.warrantyOption FROM WarrantyOptionEntity um")
	public List<String> getAllWarrantyOption();

	@Query("SELECT um FROM WarrantyOptionEntity um WHERE LOWER(um.warrantyOption)= (:warrantyName)")
	public WarrantyOptionEntity getWarrantyOption(@Param("warrantyName") String warrantyName);

}
