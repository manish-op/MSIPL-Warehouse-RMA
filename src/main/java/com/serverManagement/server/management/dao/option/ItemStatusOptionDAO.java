package com.serverManagement.server.management.dao.option;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.serverManagement.server.management.entity.options.ItemStatusOptionEntity;

public interface ItemStatusOptionDAO extends JpaRepository<ItemStatusOptionEntity, Long>{

	@Query("SELECT CASE WHEN COUNT(um) > 0 THEN TRUE ELSE FALSE END FROM ItemStatusOptionEntity um WHERE LOWER(um.itemStatus) = :optionValue")
	public boolean existsByItemStatusOptionValue(String optionValue);
	
	@Query("SELECT UPPER(um.itemStatus) FROM ItemStatusOptionEntity um")
	public List<String> getItemStatusOptionList();
	
	@Query("SELECT um FROM ItemStatusOptionEntity um WHERE LOWER(um.itemStatus) = :optionValue")
	public ItemStatusOptionEntity getItemStatusOptionDetails(String optionValue);
}
