package com.serverManagement.server.management.dao.itemRepairDetails;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.serverManagement.server.management.dto.itemRepairDetails.TicketDetailsViaIdDTO;
import com.serverManagement.server.management.entity.itemRepairDetails.ItemRepairingEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

public interface ItemRepairingDAO extends JpaRepository<ItemRepairingEntity, Long>, JpaSpecificationExecutor<ItemRepairingEntity>{

	@Query("SELECT um FROM ItemRepairingEntity um WHERE um.id=(:ticketID)")
	public ItemRepairingEntity getRepairingTicketViaTicketId(Long ticketID);
	
	@Query("SELECT um FROM ItemRepairingEntity um WHERE LOWER(um.serialNo)=(:serialNo)")
	public List<ItemRepairingEntity> getRepairingTicketViaSerialNo(String serialNo);
	
	@Query("SELECT CASE WHEN COUNT(um.region) > 0 THEN TRUE ELSE FALSE END FROM ItemRepairingEntity um WHERE um.region=(:regionEntity)")
	public boolean belongingToSameRegion(RegionEntity regionEntity);
	
	@Query("SELECT new com.serverManagement.server.management.dto.itemRepairDetails.TicketDetailsViaIdDTO(um.id,um.faultDetails, um.faultRemark, um.rmaNo, um.serialNo, um.technician_Name, um.generatedDate, um.lastUpdateDate, um.tech_Assign_Date, inPass.id, um.docketIdInward, um.docketIdOutward, um.partNo, um.ticketGeneratedBy, um.assignByManager, rp.statusOption, tech.technicianAssign , pass.id, war.warrantyOption, reg.city, itm.system, key.keywordName, subKey.subKeyword) FROM ItemRepairingEntity um LEFT JOIN um.repairStatus rp LEFT JOIN um.technicianStatus tech LEFT JOIN um.inwardGatepass pass LEFT JOIN um.outwardGatePass inPass LEFT JOIN um.warrantyDetails war LEFT JOIN um.region reg LEFT JOIN um.itemDetailId itm LEFT JOIN itm.keywordEntity key LEFT JOIN itm.subKeyWordEntity subKey WHERE um.id=(:ticketID)")
	public TicketDetailsViaIdDTO getRepairingTicketViaTicketIdInDTO(Long ticketID);
}
