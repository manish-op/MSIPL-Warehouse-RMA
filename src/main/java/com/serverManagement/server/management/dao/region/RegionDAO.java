package com.serverManagement.server.management.dao.region;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serverManagement.server.management.entity.region.RegionEntity;

public interface RegionDAO extends JpaRepository<RegionEntity, Long> {

	@Query("SELECT um from RegionEntity um WHERE LOWER(um.city) = :city")
	public RegionEntity findByCity(@Param("city") String city);

	@Query("SELECT CASE WHEN COUNT(um) > 0 THEN TRUE ELSE FALSE END FROM RegionEntity um WHERE LOWER(um.city) = :city")
	public boolean existsByCity(@Param("city") String city);

	@Query("SELECT um.city FROM RegionEntity um")
	public List<String> findAllCity();
}
