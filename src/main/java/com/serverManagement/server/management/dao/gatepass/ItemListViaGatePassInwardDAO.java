package com.serverManagement.server.management.dao.gatepass;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serverManagement.server.management.entity.gatePass.ItemListViaGatePassInward;

public interface ItemListViaGatePassInwardDAO extends JpaRepository<ItemListViaGatePassInward, Long>{

//	@QUERY("SELECT um From ItemListViaGatePassInward um WHERE um.serialNo=(:serial) ORDER BY rmaNo DESC LIMIT 1")
//	public void getData(String serial);
	
}
