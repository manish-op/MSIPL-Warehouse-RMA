package com.serverManagement.server.management.dao.itemRepairDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.serverManagement.server.management.entity.itemRepairDetails.FruEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

public interface FruDAO extends JpaRepository<FruEntity, Long> {
	
	@Query("SELECT um from FruEntity um WHERE LOWER(um.rmaNo)= (:rmaNo) AND um.regionDetails= (:regionEntity)")
	public FruEntity getRmaConfirmation(String rmaNo, RegionEntity regionEntity);

}
