package com.serverManagement.server.management.dao.itemRepairOption;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serverManagement.server.management.entity.itemRepairOption.RepairingOptionEntity;

public interface RepairingOptionDAO extends JpaRepository<RepairingOptionEntity, Long> {

	@Query("SELECT UPPER(um.statusOption) FROM RepairingOptionEntity um")
	public List<String> getAllRepairingOption();

	@Query("select um from RepairingOptionEntity um where um.statusOption=(:option)")
	public RepairingOptionEntity getOptionDetails(@Param("option") String option);
}
