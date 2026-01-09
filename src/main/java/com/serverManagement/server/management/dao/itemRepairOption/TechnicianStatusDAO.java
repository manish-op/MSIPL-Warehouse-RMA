package com.serverManagement.server.management.dao.itemRepairOption;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serverManagement.server.management.entity.itemRepairOption.TechnicianStatusEntity;

public interface TechnicianStatusDAO extends JpaRepository<TechnicianStatusEntity, Long> {

	@Query("SELECT UPPER(um.technicianAssign) FROM TechnicianStatusEntity um")
	public List<String> getAllTechnicianStatusOption();

	@Query("SELECT um FROM TechnicianStatusEntity um WHERE LOWER(um.technicianAssign)= (:technicianStatus)")
	public TechnicianStatusEntity getTechnicianStatus(@Param("technicianStatus") String technicianStatus);

}
