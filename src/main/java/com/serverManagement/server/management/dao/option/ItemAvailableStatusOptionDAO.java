package com.serverManagement.server.management.dao.option;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;

public interface ItemAvailableStatusOptionDAO extends JpaRepository<ItemAvailableStatusOptionEntity, Long> {

	@Query("SELECT CASE WHEN COUNT(um) > 0 THEN TRUE ELSE FALSE END FROM ItemAvailableStatusOptionEntity um WHERE LOWER(um.itemAvailableOption) =:optionValue")
	public boolean existsByOptionValue(String optionValue);
	
	@Query("SELECT UPPER(um.itemAvailableOption) FROM ItemAvailableStatusOptionEntity um")
	public List<String> getStatusOptionList();
	
	@Query("SELECT um FROM ItemAvailableStatusOptionEntity um WHERE LOWER(um.itemAvailableOption) =:optionValue")
	public ItemAvailableStatusOptionEntity getStatusDetailsByOption(String optionValue);
}
